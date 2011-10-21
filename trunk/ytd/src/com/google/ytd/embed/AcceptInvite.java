/* Copyright (c) 2011 Google Inc.
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
import java.net.URLDecoder;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.NamespaceManager;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.ytd.util.Util;


@Singleton
public class AcceptInvite extends HttpServlet {
  private static final Logger log = Logger.getLogger(AcceptInvite.class.getName());

  @Inject
  private Util util;

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String confirmation = request.getParameter("confirmation");
    if (util.isNullOrEmpty(confirmation)) {
      throw new IllegalArgumentException("Required parameter 'confirmation' is missing.");
    }
    confirmation = URLDecoder.decode(confirmation, "UTF-8");
    
    UserService userService = UserServiceFactory.getUserService();
    User user = userService.getCurrentUser();
    if (user == null) {
      response.sendRedirect(userService.createLoginURL(util.getSelfUrl(request)));
      return;
    }
    String email = user.getEmail().toLowerCase();
    
    NamespaceManager.set("nsadmin");
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Query query = new Query("NamespaceToUserMapping");
    query.addFilter("email", Query.FilterOperator.EQUAL, email);
    query.addFilter("confirmation", Query.FilterOperator.EQUAL, confirmation);
    PreparedQuery preparedQuery = datastore.prepare(query);
    Entity entity = preparedQuery.asSingleEntity();
    
    if (entity == null) {
      throw new IllegalArgumentException(String.format("No invitation was found for '%s' " +
      		"with confirmation code '%s'.", email, confirmation));
    }
    
    Boolean confirmed = (Boolean) entity.getProperty("confirmed");
    if (!confirmed) {
      entity.setProperty("confirmed", true);
      entity.setProperty("user", user);
      datastore.put(entity);
    }
    
    String namespace = (String) entity.getProperty("namespace");
    log.info(String.format("'%s' accepted invitation with code '%s' and is now an admin of '%s'.",
      email, confirmation, namespace));
    
    util.removeFromCache(namespace);

    response.sendRedirect(util.addNamespaceParam("/admin", namespace));
  }
}
