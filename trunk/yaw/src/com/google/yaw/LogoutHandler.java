package com.google.yaw;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.yaw.model.UserSession;

/**
 * Simple servlet to handle logging a user out.
 * 
 */
@SuppressWarnings("serial")
public class LogoutHandler extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

    UserSession userSession = UserSessionManager.getUserSession(req);

    // Don't revoke the AuthSub token, since that's needed for branding the video after moderation.
    // If the user wants to revoke their token, they can do it from youtube.com.
    
    // Remove local cookie.
    UserSessionManager.destroySessionIdCookie(resp);

    // Get the original URL to redirect.
    String redirectUrl = userSession.getMetaData("selfUrl");

    // Remove the session entry.
    UserSessionManager.delete(userSession);

    // Send the redirect to our original URL.
    resp.sendRedirect(redirectUrl);
  }
}