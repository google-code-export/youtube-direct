package com.google.ytd.jsonrpc;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.ytd.Util;
import com.google.ytd.command.Command;

@Singleton
public class JsonRpcProcessor extends HttpServlet {
  private static final Logger LOG = Logger.getLogger(JsonRpcProcessor.class.getName());
  @Inject
  private CommandDirectory commandDirectory;

  @Inject
  private JsonExceptionHandler jsonExceptionHandler;

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    try {
      String postBody = Util.getPostBody(req);
      if (Util.isNullOrEmpty(postBody)) {
        throw new IllegalArgumentException("No data found in HTTP POST request.");
      }

      Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
      JsonRpcRequest jsonRpcRequest = gson.fromJson(postBody, JsonRpcRequest.class);

      if (jsonRpcRequest != null) {
        String method = jsonRpcRequest.getMethod();
        if (method != null) {
          Command command = commandDirectory.getCommand(method, jsonRpcRequest.getParams());
          resp.setContentType("application/json");
          try {
            JSONObject json = command.execute();
            json.put("error", "null");
            resp.getWriter().write(json.toString());
          } catch (JSONException e) {
            jsonExceptionHandler.send(resp, e);
          } catch (IllegalArgumentException e) {
            jsonExceptionHandler.send(resp, e);
          }
        }
      }
    } catch(RuntimeException e) {
      jsonExceptionHandler.send(resp, e);
    }
  }
}
