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
import java.security.PrivateKey;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.NamespaceManager;
import com.google.gdata.client.http.AuthSubUtil;
import com.google.gdata.util.AuthenticationException;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.ytd.dao.AdminConfigDao;
import com.google.ytd.model.UserSession;
import com.google.ytd.util.PmfUtil;
import com.google.ytd.util.Util;

/**
 * Class that manages UserSession objects.
 */
@Singleton
public class UserSessionManager {
  private static final String USER_SESSION_ID_NAME = "YTD_SESSION_ID";
  private Util util = null;
  private PmfUtil pmfUtil = null;
  private PersistenceManagerFactory pmf = null;
  private AdminConfigDao adminConfigDao = null;

  @Inject
  public UserSessionManager(PersistenceManagerFactory pmf, AdminConfigDao adminConfigDao,
      Util util, PmfUtil pmfUtil) {
    this.pmf = pmf;
    this.adminConfigDao = adminConfigDao;
    this.util = util;
    this.pmfUtil = pmfUtil;
  }

  public void sendSessionIdCookie(String sessionId, HttpServletResponse response) {
    disableHttpCaching(response);
    
    Cookie cookie = new Cookie(USER_SESSION_ID_NAME, sessionId);
    // cookie lives for a year
    cookie.setMaxAge(31536000);
    response.addCookie(cookie);
  }

  public void destroySessionIdCookie(HttpServletResponse response) {
    disableHttpCaching(response);
    
    Cookie cookie = new Cookie(USER_SESSION_ID_NAME, "");
    cookie.setMaxAge(0);
    response.addCookie(cookie);
  }
  
  private void disableHttpCaching(HttpServletResponse response) {
    response.setHeader("Expires", "Mon, 01 Jan 1990 00:00:00 GMT");
    response.setHeader("Pragma", "no-cache");
    response.setHeader("Cache-control", "no-cache, no-store, must-revalidate");
  }

  public boolean isSessionValid(UserSession session) {
    boolean valid = true;

    String authSubToken = session.getMetaData("authSubToken");

    if (authSubToken != null) {
      try {
        PrivateKey privateKey = adminConfigDao.getPrivateKey();
        AuthSubUtil.getTokenInfo(authSubToken, privateKey);
      } catch (AuthenticationException e) {
        valid = false;
      } catch (IOException e) {
        valid = false;
      } catch (GeneralSecurityException e) {
        valid = false;
      }
    } else {
      valid = false;

    }
    return valid;
  }

  public UserSession save(UserSession session) {
    String oldNamespace = NamespaceManager.get();
    NamespaceManager.set("");
    UserSession userSession = (UserSession) pmfUtil.persistJdo(session);
    NamespaceManager.set(oldNamespace);
    return userSession;
  }

  public void delete(UserSession session) {
    String oldNamespace = NamespaceManager.get();
    NamespaceManager.set("");
    pmfUtil.removeJdo(session);
    NamespaceManager.set(oldNamespace);
  }

  @SuppressWarnings("unchecked")
  public UserSession getUserSession(HttpServletRequest request) {
    UserSession userSession = null;

    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if (USER_SESSION_ID_NAME.equals(cookie.getName())) {
          String sessionId = cookie.getValue();
          userSession = getUserSessionById(sessionId);
        }
      }
    }

    // Fall back on checking the sessionId parameter if cookies are disabled.
    if (userSession == null) {
      String sessionId = request.getParameter("sessionId");
      if (!util.isNullOrEmpty(sessionId)) {
        userSession = getUserSessionById(sessionId);
      }
    }

    return userSession;
  }

  @SuppressWarnings("unchecked")
  public UserSession getUserSessionById(String id) {
    String oldNamespace = NamespaceManager.get();
    NamespaceManager.set("");
    
    PersistenceManager pm = pmf.getPersistenceManager();
    UserSession userSession = null;

    String filters = "id == id_";
    Query query = pm.newQuery(UserSession.class, filters);
    query.declareParameters("String id_");
    List<UserSession> list = (List<UserSession>) query.executeWithArray(new Object[] { id });

    if (list.size() > 0) {
      userSession = list.get(0);
      userSession = pm.detachCopy(userSession);
    }

    pm.close();
    
    NamespaceManager.set(oldNamespace);

    return userSession;
  }
}
