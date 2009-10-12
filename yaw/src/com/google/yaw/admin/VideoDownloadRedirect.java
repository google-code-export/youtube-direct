package com.google.yaw.admin;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is a placeholder servlet. It is intentionally a no-op.
 */
public class VideoDownloadRedirect extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    
    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "This functionality is not " +
    		"implemented.");
  }
}