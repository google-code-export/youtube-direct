package com.google.yaw.admin;

import com.google.yaw.Util;
import com.google.yaw.model.AdminConfig;
import com.google.yaw.model.VideoSubmission;

import java.io.IOException;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet that persists settings passed as parameters into the datastore.
 */
public class GetAdminConfig extends HttpServlet {
  private static final Logger log = Logger.getLogger(GetAdminConfig.class.getName());

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    
    AdminConfig adminConfig = Util.getAdminConfig();    
    
    if (adminConfig != null) {     
      resp.setContentType("text/javascript");
      resp.getWriter().println(Util.GSON.toJson(adminConfig));     
    } else {
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
          "failed to retrieve AdminConfig");  
    }
  }
}