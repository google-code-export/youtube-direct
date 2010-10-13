package com.google.ytd.youtube;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Singleton;

/**
 * Servlet that retrieves the video download link for a video and redirects the browser there.
 * 
 * Video download links are only available for organizations who have partnered with YouTube and
 * have their own "strong authentication" key.
 */
@Singleton
public class VideoDownloadRedirect extends HttpServlet {
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "This functionality is not available.");
  }
}