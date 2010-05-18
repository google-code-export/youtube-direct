package com.google.ytd.youtube;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.ytd.dao.AdminConfigDao;
import com.google.ytd.util.Util;

/**
 * Servlet that retrieves the video download link for a video and redirects the browser there.
 * 
 * Video download links are only available for organizations who have partnered with YouTube and
 * have their own "strong authentication" key.
 */
@Singleton
public class VideoDownloadRedirect extends HttpServlet {
  private static final Logger log = Logger.getLogger(VideoDownloadRedirect.class.getName());
  private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
  
  private Util util = null;
  private AdminConfigDao adminConfigDao = null;
  
  @Inject
  public VideoDownloadRedirect(Util util, AdminConfigDao adminConfigDao) {
    this.util = util;
    this.adminConfigDao = adminConfigDao;
  }  
  
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    // no-op
  }
}