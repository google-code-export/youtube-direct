package com.google.yaw.mobile;

import com.google.gdata.client.http.AuthSubUtil;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;
import com.google.yaw.Util;
import com.google.yaw.YouTubeApiManager;
import com.google.yaw.admin.PersistAuthSubToken;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * AuthSub redirection flow for mobile phones.
 * 
 * After getting the AuthSub token, exchanges it for a session token and opens a custom URL.
 */
public class MobileAuthSub extends HttpServlet {
  private static final Logger log = Logger.getLogger(PersistAuthSubToken.class.getName());
  private static final String AUTH_SUB_FORMAT = "https://www.google.com/accounts/AuthSubRequest?" +
      "next=%s&scope=http://gdata.youtube.com&session=1&secure=0&nomobile=1";
  private static final String REDIRECT_FORMAT = "%s://authsub/%s/%s";

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    try {
      // We need to know what the redirection protocol will be whether this is an initial request
      // or a response back from the AuthSub flow.
      String protocol = req.getParameter("protocol");
      if (Util.isNullOrEmpty(protocol)) {
        throw new IllegalArgumentException("'protocol' parameter is null or empty.");
      }
      
      String token = req.getParameter("token");

      if (Util.isNullOrEmpty(token)) {
        // If there is no token URL parameter, start the AuthSub request flow.
        resp.sendRedirect(String.format(AUTH_SUB_FORMAT, Util.getSelfUrl(req)));
      } else {
        String sessionToken = AuthSubUtil.exchangeForSessionToken(token, null);
        
        // Test the token to make sure it's valid, and get the username it corresponds to.
        YouTubeApiManager apiManager = new YouTubeApiManager();
        apiManager.setToken(sessionToken);

        String youTubeName = apiManager.getCurrentUsername();
        if (Util.isNullOrEmpty(youTubeName)) {
          // TODO: Throw a better Exception class here.
          throw new IllegalArgumentException("Unable to retrieve a YouTube username for the " +
          		"authenticated user.");
        }
        
        // Redirect to the custom URL scheme, which would presumably be handled by an application
        // on the mobile phone.
        resp.sendRedirect(String.format(REDIRECT_FORMAT, protocol, youTubeName, sessionToken));
      }
    } catch (AuthenticationException e) {
      log.log(Level.WARNING, "", e);
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
    } catch (GeneralSecurityException e) {
      log.log(Level.WARNING, "", e);
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
    } catch (IllegalArgumentException e) {
      log.log(Level.WARNING, "", e);
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
    } catch (ServiceException e) {
      log.log(Level.WARNING, "", e);
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }
}