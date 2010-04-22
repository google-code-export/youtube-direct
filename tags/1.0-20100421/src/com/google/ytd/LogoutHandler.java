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

package com.google.ytd;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.ytd.model.UserSession;

/**
 * Simple servlet to handle logging a user out.
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