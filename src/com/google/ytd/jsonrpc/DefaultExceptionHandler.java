package com.google.ytd.jsonrpc;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Guice;
import com.google.inject.Singleton;
import com.google.ytd.guice.ProductionModule;

@Singleton
public class DefaultExceptionHandler extends HttpServlet {
  private static final Logger LOG = Logger.getLogger(DefaultExceptionHandler.class.getName());

  JsonExceptionHandler jsonExceptionHandler =
      Guice.createInjector(new ProductionModule()).getInstance(JsonExceptionHandler.class);

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    sendError(req, resp);
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    sendError(req, resp);
  }

  public void sendError(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String message = (String) req.getAttribute("javax.servlet.error.message");
    if (req.getAttribute("javax.servlet.error.status_code") != null) {
      Integer statusCode = (Integer) req.getAttribute("javax.servlet.error.status_code");
      message = statusCode + ": " + message;
    }
    jsonExceptionHandler.send(resp, message);
  }
}
