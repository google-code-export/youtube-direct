package com.google.yaw;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gdata.client.http.AuthSubUtil;
import com.google.gdata.util.ServiceException;
import com.google.yaw.model.UserSession;

/**
 * Super simple class to handle doing the AuthSub token exchange to upgrade a
 * one-time token into a session token.
 */
public class AuthSubHandler extends HttpServlet {
  private static final Logger log = Logger.getLogger(AuthSubHandler.class.getName());

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String token = AuthSubUtil.getTokenFromReply(request.getQueryString());

    try {
      if (token == null) {
        throw new IllegalArgumentException(String.format("Could not retrieve token from "
            + "AuthSub response. request.getQueryString() => %s", request.getQueryString()));
      }

      String articleUrl = request.getParameter("articleUrl");
      if (Util.isNullOrEmpty(articleUrl)) {
        throw new IllegalArgumentException("'articleUrl' parameter is null or empty.");
      }

      String authSubToken = AuthSubUtil.exchangeForSessionToken(token, null);

      UserSession userSession = UserSessionManager.getUserSession(request);

      if (userSession == null) {
        // TODO: Throw a better Exception class here.
        throw new IllegalArgumentException("No user session found.");
      }

      //userSession.setAuthSubToken(authSubToken);
      userSession.addMetaData("authSubToken", authSubToken);

      YouTubeApiManager apiManager = new YouTubeApiManager();
      apiManager.setToken(authSubToken);

      String youTubeName = apiManager.getCurrentUsername();
      if (Util.isNullOrEmpty(youTubeName)) {
        // TODO: Throw a better Exception class here.
        throw new IllegalArgumentException("Unable to retrieve a YouTube username for "
            + "the authenticated user.");
      }
      userSession.addMetaData("youTubeName", youTubeName);
      UserSessionManager.save(userSession);

      response.sendRedirect(articleUrl + "#return");
    } catch (ServiceException e) {
      log.log(Level.WARNING, "", e);
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
    } catch (IllegalArgumentException e) {
      log.log(Level.WARNING, "", e);
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
    } catch (GeneralSecurityException e) {
      log.log(Level.WARNING, "", e);
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Security error while " +
      		"retrieving session token.");
    }
  }
}
