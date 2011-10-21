package com.google.ytd.jsonrpc;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.appengine.api.NamespaceManager;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.ytd.command.Command;
import com.google.ytd.command.CommandType;
import com.google.ytd.command.NonAdmin;
import com.google.ytd.util.Util;

@Singleton
public class JsonRpcProcessor extends HttpServlet {
  private JsonExceptionHandler jsonExceptionHandler;
  private Util util;
  private Injector injector;

  @Inject
  public JsonRpcProcessor(JsonExceptionHandler jsonExceptionHandler, Util util, Injector injector) {
    this.jsonExceptionHandler = jsonExceptionHandler;
    this.util = util;
    this.injector = injector;
  }

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
          Class<? extends Command> commandClass = CommandType.valueOfIngoreCase(method).getClazz();

          UserService userService = UserServiceFactory.getUserService();

          if (commandClass.isAnnotationPresent(NonAdmin.class) || userService.isUserAdmin() ||
              util.isUserPermissionedForNamespace(userService.getCurrentUser(),
                NamespaceManager.get())) {
            Command command = injector.getInstance(commandClass);
            command.setParams(jsonRpcRequest.getParams());

            try {
              JSONObject json = command.execute();
              resp.setContentType("application/json; charset=UTF-8");
              resp.getWriter().write(json.toString());
            } catch (JSONException e) {
              jsonExceptionHandler.send(resp, e);
            } catch (IllegalArgumentException e) {
              jsonExceptionHandler.send(resp, e);
            }
          } else {
            jsonExceptionHandler.send(resp, String.format("The current user can't access " +
            		"method '%s' using namespace '%s'.", method, NamespaceManager.get()));
          }
        }
      }
    } catch (RuntimeException e) {
      jsonExceptionHandler.send(resp, e);
    }
  }
}
