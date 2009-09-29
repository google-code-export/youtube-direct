package com.google.yaw.admin;

import com.google.gdata.client.http.AuthSubUtil;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;
import com.google.yaw.Util;
import com.google.yaw.YouTubeApiManager;
import com.google.yaw.model.AdminConfig;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * AuthSub redirection flow to persist the token belonging to the admin YouTube account.
 */
public class PersistAuthSubToken extends HttpServlet {
  private static final Logger log = Logger.getLogger(PersistAuthSubToken.class.getName());

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    PersistenceManager pm = Util.getPersistenceManagerFactory().getPersistenceManager();

    try {
      String token = AuthSubUtil.getTokenFromReply(req.getQueryString());
      if (Util.isNullOrEmpty(token)) {
        throw new IllegalArgumentException(String.format("Could not retrieve token from "
                + "AuthSub response. request.getQueryString() => %s", req.getQueryString()));
      }

      String sessionToken = AuthSubUtil.exchangeForSessionToken(token, null);

      YouTubeApiManager apiManager = new YouTubeApiManager();
      apiManager.setToken(sessionToken);

      String youTubeName = apiManager.getCurrentUsername();
      if (Util.isNullOrEmpty(youTubeName)) {
        // TODO: Throw a better Exception class here.
        throw new IllegalArgumentException("Unable to retrieve a YouTube username for "
                + "the authenticated user.");
      }

      AdminConfig adminConfig = Util.getAdminConfig();
      adminConfig.setYouTubeAuthSubToken(sessionToken);
      adminConfig.setYouTubeUsername(youTubeName);

      pm.makePersistent(adminConfig);

      resp.sendRedirect("/admin#configuration");
    } catch (IllegalArgumentException e) {
      log.log(Level.WARNING, "", e);
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
    } catch (AuthenticationException e) {
      log.log(Level.WARNING, "", e);
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
    } catch (GeneralSecurityException e) {
      log.log(Level.WARNING, "", e);
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
    } catch (ServiceException e) {
      log.log(Level.WARNING, "", e);
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
    } finally {
      pm.close();
    }
  }
}