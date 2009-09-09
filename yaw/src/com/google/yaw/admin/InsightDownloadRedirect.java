package com.google.yaw.admin;

import com.google.gdata.data.Link;
import com.google.gdata.data.youtube.VideoEntry;
import com.google.yaw.Util;
import com.google.yaw.YouTubeApiManager;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet that retrieves the Insight download link for a video and redirects the browser there.
 * 
 * The Insight download link is generated with a time-sensitive parameter, so it can't be cached.
 * More info on Insight data is available at
 * http://code.google.com/apis/youtube/2.0/developers_guide_protocol_insight.html
 */
public class InsightDownloadRedirect extends HttpServlet {
  private static final Logger log = Logger.getLogger(InsightDownloadRedirect.class.getName());

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    try {
      String id = req.getParameter("id");
      if (Util.isNullOrEmpty(id)) {
        throw new IllegalArgumentException("'id' parameter is null or empty.");
      }
      
      String token = req.getParameter("token");
      if (Util.isNullOrEmpty(token)) {
        throw new IllegalArgumentException("'token' parameter is null or empty.");
      }

      YouTubeApiManager apiManager = new YouTubeApiManager();
      apiManager.setToken(token);
      
      VideoEntry videoEntry = apiManager.getVideoEntry(id);
      if (videoEntry == null) {
        throw new IllegalArgumentException(String.format("Couldn't retrieve video entry with id " +
        		"'%s' using token '%s'.", id, token));
      }
      
      Link insightLink = videoEntry.getLink("http://gdata.youtube.com/schemas/2007#insight.views",
              null);
      if (insightLink != null) {
        String url = insightLink.getHref();
        if (Util.isNullOrEmpty(url)) {
          throw new IllegalArgumentException(String.format("No insight download URL found for " +
          		"video id '%s'.", id));
        }
        
        resp.sendRedirect(url);
      } else {
        throw new IllegalArgumentException(String.format("No insight download link found for " +
        		"video id '%s'.", id));
      }
    } catch (IllegalArgumentException e) {
      log.log(Level.WARNING, "", e);
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
    }
  }
}