/* Copyright (c) 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.ytd.embed;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManagerFactory;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gdata.client.http.AuthSubUtil;
import com.google.gdata.util.ServiceException;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.ytd.YouTubeApiManager;
import com.google.ytd.dao.UserAuthTokenDao;
import com.google.ytd.model.UserSession;
import com.google.ytd.util.Util;

/**
 * Super simple class to handle doing the AuthSub token exchange to upgrade a
 * one-time token into a session token.
 */
@Singleton
public class AuthSubHandler extends HttpServlet {
  private static final Logger log = Logger.getLogger(AuthSubHandler.class.getName());

  @Inject
  private Util util;
  @Inject
  private PersistenceManagerFactory pmf;
  @Inject
  private UserSessionManager userSessionManager;
  @Inject
  private YouTubeApiManager apiManager;
  @Inject
  private UserAuthTokenDao userAuthTokenDao;

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String token = AuthSubUtil.getTokenFromReply(request.getQueryString());

    try {
      if (token == null) {
        throw new IllegalArgumentException(String.format("Could not retrieve token from "
            + "AuthSub response. request.getQueryString() => %s", request.getQueryString()));
      }

      String articleUrl = request.getParameter("articleUrl");
      if (util.isNullOrEmpty(articleUrl)) {
        throw new IllegalArgumentException("'articleUrl' parameter is null or empty.");
      }

      String authSubToken = AuthSubUtil.exchangeForSessionToken(token, null);

      UserSession userSession = userSessionManager.getUserSession(request);

      if (userSession == null) {
        // TODO: Throw a better Exception class here.
        throw new IllegalArgumentException("No user session found.");
      }

      //userSession.setAuthSubToken(authSubToken);
      userSession.addMetaData("authSubToken", authSubToken);

      apiManager.setToken(authSubToken);

      String youTubeName = apiManager.getCurrentUsername();
      if (util.isNullOrEmpty(youTubeName)) {
        // TODO: Throw a better Exception class here.
        throw new IllegalArgumentException("Unable to retrieve a YouTube username for "
            + "the authenticated user.");
      }
      userSession.addMetaData("youTubeName", youTubeName);
      userSessionManager.save(userSession);

      // Create or update the UserAuthToken entry, which maps a username to an AuthSub token.
      userAuthTokenDao.setUserAuthToken(youTubeName, authSubToken);

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
