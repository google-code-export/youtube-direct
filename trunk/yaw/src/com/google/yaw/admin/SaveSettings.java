package com.google.yaw.admin;

import com.google.yaw.Util;
import com.google.yaw.model.Settings;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet that persists settings passed as parameters into the datastore.
 */
public class SaveSettings extends HttpServlet {
  private static final Logger log = Logger.getLogger(GetOptionsHTML.class.getName());

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    Settings settings = Util.getSettings();

    settings.setClientId(req.getParameter("clientId"));
    settings.setDeveloperKey(req.getParameter("developerKey"));
    
    Util.persistJdo(settings);
    
    //TODO: Do something friendlier here.
    resp.sendRedirect("/admin");
  }
}