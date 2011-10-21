// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.ytd.namespace;

import com.google.appengine.api.NamespaceManager;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.ytd.util.Util;

import java.io.IOException;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class NamespaceFilter implements Filter {
  private static final Util util = Util.get();

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
      throws IOException, ServletException {
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;
    String namespace = httpRequest.getParameter("ns");
    String requestUri = httpRequest.getRequestURI().toLowerCase();
    
    if (requestUri.contains("admin") && !requestUri.startsWith("/_ah/")) {
      UserService userService = UserServiceFactory.getUserService();
      User currentUser = userService.getCurrentUser();
      
      if (namespace == null) {
        List<String> authorizedNamespaces = util.getAuthorizedNamespacesForUser(currentUser);
        switch (authorizedNamespaces.size()) {
          case 0:
            throw new ServletException("The 'ns' parameter is missing, and the user isn't" +
            		" permissioned for any namespaces.");
          
          case 1:
            httpResponse.sendRedirect(util.addNamespaceParam("/admin",
              authorizedNamespaces.get(0)));
            return;
          
          default:
            httpResponse.sendRedirect("/choose_instance.html?instances=" +
                util.sortedJoin(authorizedNamespaces, ","));
            return;
        }
      }

      if (currentUser == null) {
        throw new ServletException("You must be logged in to a Google Account.");
      }
      
      if (!util.isUserPermissionedForNamespace(currentUser, namespace)) {
        throw new ServletException(String.format("Error while requesting '%s'. The user '%s' " +
            "is not allowed to access namespace '%s'.", httpRequest.getRequestURI(),
            currentUser.getEmail(), namespace));
      }
    }
    
    if (namespace != null) {
      NamespaceManager.set(namespace);
    }

    filterChain.doFilter(request, response);
  }

  @Override
  public void init(FilterConfig arg0) {
    // no-op
  }

  @Override
  public void destroy() {
    // no-op
  }
}
