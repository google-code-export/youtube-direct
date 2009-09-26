package com.google.yaw.admin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gdata.data.youtube.VideoEntry;
import com.google.gdata.data.youtube.YouTubeMediaGroup;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;
import com.google.yaw.Util;
import com.google.yaw.YouTubeApiManager;
import com.google.yaw.model.AdminConfig;
import com.google.yaw.model.Assignment;
import com.google.yaw.model.VideoSubmission;
import com.google.yaw.model.AdminConfig.BrandingModeType;
import com.google.yaw.model.VideoSubmission.ModerationStatus;
import com.google.yaw.model.VideoSubmission.VideoSource;

/**
 * Servlet responsible for creating new assignment
 */
public class NewAssignment extends HttpServlet {
  private static final Logger log = Logger.getLogger(NewAssignment.class.getName());

  @SuppressWarnings("cast")
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    PersistenceManagerFactory pmf = Util.getPersistenceManagerFactory();
    PersistenceManager pm = pmf.getPersistenceManager();

    try {
      String json = Util.getPostBody(req);

      Assignment entry = null;

      Assignment jsonObj = Util.GSON.fromJson(json, Assignment.class);

      entry = new Assignment();
      entry.setStatus(jsonObj.getStatus());
      entry.setDescription(jsonObj.getDescription());
      entry.setCategory(jsonObj.getCategory());
      
      pm.makePersistent(entry);

      resp.setContentType("text/javascript");
      resp.getWriter().println(Util.GSON.toJson(entry));      
      
    } finally {
      pm.close();
    }
  }
  
}
