/* Copyright (c) 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.ytd.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.repackaged.com.google.common.util.Base64;
import com.google.appengine.repackaged.com.google.common.util.Base64DecoderException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.inject.Singleton;

/**
 * Misc. utility methods.
 */
@Singleton
public class Util {
  public static final String CLIENT_ID_PREFIX = "ytd23-";
  private static final String DATE_TIME_PATTERN = "EEE, d MMM yyyy HH:mm:ss Z";

  public final Gson GSON = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setDateFormat(
      DATE_TIME_PATTERN).registerTypeAdapter(Text.class, new TextToStringAdapter())
      .registerTypeAdapter(Blob.class, new BlobToStringAdapter()).create();

  private static class TextToStringAdapter implements JsonSerializer<Text>, JsonDeserializer<Text> {
    @SuppressWarnings("unused")
    public JsonElement toJson(Text text, Type type, JsonSerializationContext context) {
      return serialize(text, type, context);
    }

    @SuppressWarnings("unused")
    public Text fromJson(JsonElement json, Type type, JsonDeserializationContext context) {
      return deserialize(json, type, context);
    }

    public JsonElement serialize(Text text, Type type, JsonSerializationContext context) {
      return new JsonPrimitive(text.getValue());
    }

    public Text deserialize(JsonElement json, Type type, JsonDeserializationContext context) {
      try {
        return new Text(json.getAsString());
      } catch (JsonParseException e) {
        // TODO: This is kind of a hacky way of reporting back a parse
        // exception.
        return new Text(e.toString());
      }
    }
  }

  private static class BlobToStringAdapter implements JsonSerializer<Blob>, JsonDeserializer<Blob> {
    @SuppressWarnings("unused")
    public JsonElement toJson(Blob blob, Type type, JsonSerializationContext context) {
      return serialize(blob, type, context);
    }

    @SuppressWarnings("unused")
    public Blob fromJson(JsonElement json, Type type, JsonDeserializationContext context) {
      return deserialize(json, type, context);
    }

    public JsonElement serialize(Blob blob, Type type, JsonSerializationContext context) {
      return new JsonPrimitive(Base64.encode(blob.getBytes()));
    }

    public Blob deserialize(JsonElement json, Type type, JsonDeserializationContext context) {
      try {
        return new Blob(Base64.decode(json.getAsString()));
      } catch (JsonParseException e) {
        // TODO: This is kind of a hacky way of reporting back a parse exception.
        return new Blob(e.toString().getBytes());
      } catch (Base64DecoderException e) {
        // TODO: This is kind of a hacky way of reporting back a parse exception.
        return new Blob(e.toString().getBytes());
      }
    }
  }

  private static Util util = null;

  public static Util get() {
    if (util == null) {
      return new Util();
    } else {
      return util;
    }
  }

  public String getPostBody(HttpServletRequest req) throws IOException {
    InputStream is = req.getInputStream();
    
    return readInputStream(is);
  }
  
  public String readInputStream(InputStream inputStream) throws IOException {
    StringBuffer body = new StringBuffer();
    String line = null;
    BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
    while ((line = br.readLine()) != null) {
      body.append(line);
      body.append("\n");
    }
    
    return body.toString();
  }

  public String getSelfUrl(HttpServletRequest request) {
    StringBuffer url = new StringBuffer();

    url.append(request.getRequestURL());
    String queryString = request.getQueryString();
    if (!isNullOrEmpty(queryString)) {
      url.append("?");
      url.append(queryString);
    }

    return url.toString();
  }

  public boolean isNullOrEmpty(String input) {
    if (input == null || input.length() <= 0) {
      return true;
    } else {
      return false;
    }
  }

  public String toJson(Object o) {
    return GSON.toJson(o);
  }

  /**
   * Sorts a list and then performs a join into one large string, using the
   * delimeter specified.
   * 
   * @param strings
   *          The list of strings to sort and join.
   * @param delimeter
   *          The delimeter string to insert in between each string in the list.
   * @return A string consisting of a sorted list of strings, joined with
   *         delimeter.
   */
  public String sortedJoin(List<String> strings, String delimeter) {
    Collections.sort(strings);

    StringBuffer tempBuffer = new StringBuffer();
    for (int i = 0; i < strings.size(); i++) {
      tempBuffer.append(strings.get(i));
      if (i < strings.size() - 1) {
        tempBuffer.append(delimeter);
      }
    }

    return tempBuffer.toString();
  }
}
