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
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManagerFactory;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gdata.client.http.AuthSubUtil;
import com.google.gdata.util.AuthenticationException;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.ytd.dao.AdminConfigDao;
import com.google.ytd.model.UserSession;
import com.google.ytd.util.Util;

/**
 * Class to handle aspects of user authentication and persisting that
 * information in UserSession objects.
 */
@Singleton
public class Authenticator {
  private Util util;
  @Inject
  private PersistenceManagerFactory pmf;

  @Inject
  private AdminConfigDao adminConfigDao;

  private UserSessionManager userSessionManager;

  private HttpServletRequest request = null;
  private HttpServletResponse response = null;

  private static final String SCOPE = "http://gdata.youtube.com";
  private static final String AUTHSUB_HANDLER = "/AuthsubHandler";

  private UserSession userSession = null;

  private static final Logger log = Logger.getLogger(Authenticator.class.getName());

  @Inject
  public Authenticator(HttpServletRequest req, HttpServletResponse resp,
      UserSessionManager userSessionManager, Util util, AdminConfigDao adminConfigDao) {
    this.userSessionManager = userSessionManager;
    this.request = req;
    this.response = resp;
    this.util = util;
    this.adminConfigDao = adminConfigDao;
    this.userSession = userSessionManager.getUserSession(request);

    String assignmentId = request.getParameter("assignmentId");
    String articleUrl = request.getParameter("articleUrl");
    try {
      // This URL string was encoded by JavaScript with escape()
      articleUrl = URLDecoder.decode(articleUrl, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      log.log(Level.WARNING, "", e);
    }

    // TODO(austinchau) This request object is before the JspForwarder, the url
    // is not correct
    String selfUrl = util.getSelfUrl(request);

    if (userSession == null) {
      userSession = new UserSession();
      userSession = userSessionManager.save(userSession);
      // stick the session id as cookie
      userSessionManager.sendSessionIdCookie(userSession.getId(), response);
    }

    userSession.addMetaData("assignmentId", assignmentId);
    userSession.addMetaData("articleUrl", articleUrl);
    userSession.addMetaData("selfUrl", selfUrl);

    userSession = userSessionManager.save(userSession);

    String authSubToken = userSession.getMetaData("authSubToken");

    if (authSubToken != null) {
      // check for bad token
      if (!isTokenValid(authSubToken)) {
        log.finer(String.format("AuthSub token '%s' is invalid. Creating new session.",
            authSubToken));

        authSubToken = null;

        userSessionManager.delete(userSession);

        // replace with new session

        userSession = new UserSession();
        userSession.addMetaData("assignmentId", assignmentId);
        userSession.addMetaData("articleUrl", articleUrl);
        userSession.addMetaData("selfUrl", selfUrl);
        userSession = userSessionManager.save(userSession);

        // stick the session id as cookie
        userSessionManager.sendSessionIdCookie(userSession.getId(), response);
      } else {
        // good token
        log.finest(String.format("Reusing cached AuthSub token '%s'.", authSubToken));
      }
    }
  }

  public boolean isTokenValid(String token) {
    try {
      PrivateKey privateKey = adminConfigDao.getPrivateKey();
      AuthSubUtil.getTokenInfo(token, privateKey);
    } catch (AuthenticationException e) {
      return false;
    } catch (IOException e) {
      return false;
    } catch (GeneralSecurityException e) {
      return false;
    }
    return true;
  }

  public boolean isLoggedIn() {
    boolean isLoggedIn = false;
    String authSubToken = userSession.getMetaData("authSubToken");

    if (authSubToken != null && isTokenValid(authSubToken)) {
      isLoggedIn = true;
    }

    return isLoggedIn;
  }

  public UserSession getUserSession() {
    return this.userSession;
  }

  public String getLogInUrl() {
    String loginUrl = null;
    String articleUrl = userSession.getMetaData("articleUrl");

    try {
      articleUrl = URLEncoder.encode(articleUrl, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      log.log(Level.WARNING, "", e);
    }

    StringBuffer nextUrl = new StringBuffer();
    nextUrl.append(request.getScheme());
    nextUrl.append("://");
    nextUrl.append(request.getServerName());
    if (request.getServerPort() != 80) {
      nextUrl.append(":").append(request.getServerPort());
    }
    nextUrl.append(AUTHSUB_HANDLER);
    nextUrl.append("?articleUrl=");
    nextUrl.append(articleUrl);
    
    // Insert the session id as a parameter, to support browsers with cookies disabled.
    nextUrl.append("&sessionId=");
    nextUrl.append(getUserSession().getId());

    boolean secure = adminConfigDao.getPrivateKey() != null;
    loginUrl = AuthSubUtil.getRequestUrl(nextUrl.toString(), SCOPE, secure, true);

    return loginUrl;
  }

  public String getLogOutUrl() {
    return "/logout?sessionId=" + userSession.getId();
  }
}
