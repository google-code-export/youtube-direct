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

package com.google.ytd.admin;

import java.io.IOException;
import java.util.logging.Logger;

import javax.jdo.PersistenceManagerFactory;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.ytd.model.AdminConfig;
import com.google.ytd.util.Util;

/**
 * Servlet that retrieves the AdminConfig singleton from the datastore.
 */
@Singleton
public class GetAdminConfig extends HttpServlet {
  private static final Logger log = Logger.getLogger(GetAdminConfig.class.getName());
  @Inject
  private Util util;
  @Inject
  private PersistenceManagerFactory pmf;

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    AdminConfig adminConfig = util.getAdminConfig();

    if (adminConfig != null) {
      resp.setContentType("text/javascript");
      resp.getWriter().println(util.toJson(adminConfig));
    } else {
      log.warning("Couldn't retrieve an AdminConfig instance.");
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Couldn't retrieve AdminConfig");
    }
  }
}