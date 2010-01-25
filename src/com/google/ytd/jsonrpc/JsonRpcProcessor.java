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
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.ytd.command.Command;
import com.google.ytd.command.CommandType;
import com.google.ytd.util.Util;

@Singleton
public class JsonRpcProcessor extends HttpServlet {
  private static final Logger LOG = Logger.getLogger(JsonRpcProcessor.class.getName());
  @Inject
  private JsonExceptionHandler jsonExceptionHandler;
  @Inject
  private Util util;
  @Inject
  private Injector injector;

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    try {
      String postBody = util.getPostBody(req);
      if (util.isNullOrEmpty(postBody)) {
        throw new IllegalArgumentException("No data found in HTTP POST request.");
      }

      Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
      JsonRpcRequest jsonRpcRequest = gson.fromJson(postBody, JsonRpcRequest.class);

      if (jsonRpcRequest != null) {
        String method = jsonRpcRequest.getMethod();
        if (method != null) {
          LOG.info("processing method " + method);
          Class<? extends Command> commandClass = CommandType.valueOfIngoreCase(method).getClazz();
          Command command = injector.getInstance(commandClass);
          command.setParams(jsonRpcRequest.getParams());

          try {
            JSONObject json = command.execute();
            // json.put("error", "null");
            resp.setContentType("application/json");
            resp.getWriter().write(json.toString());
          } catch (JSONException e) {
            jsonExceptionHandler.send(resp, e);
          } catch (IllegalArgumentException e) {
            jsonExceptionHandler.send(resp, e);
          }
        }
      }
    } catch (RuntimeException e) {
      jsonExceptionHandler.send(resp, e);
    }
  }
}
