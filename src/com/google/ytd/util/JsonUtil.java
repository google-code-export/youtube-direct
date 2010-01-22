package com.google.ytd.util;

import java.lang.reflect.Type;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Text;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class JsonUtil {
	private static final Logger log = Logger.getLogger(JsonUtil.class.getName());
	private static final String DATE_TIME_PATTERN = "EEE, d MMM yyyy HH:mm:ss Z";
	public final Gson GSON = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setDateFormat(
			DATE_TIME_PATTERN).registerTypeAdapter(Text.class, new TextToStringAdapter()).create();

	private static class TextToStringAdapter implements JsonSerializer<Text>, JsonDeserializer<Text> {
		public JsonElement toJson(Text text, Type type, JsonSerializationContext context) {
			return serialize(text, type, context);
		}

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
}
