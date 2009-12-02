package com.google.ytd.guice;

import java.io.IOException;
import java.util.logging.Logger;

import javax.jdo.PersistenceManagerFactory;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

@Singleton
public class JspForwarder extends HttpServlet {
  private static final Logger LOG = Logger.getLogger(JspForwarder.class.getName());

  @Inject
  Injector injector;

  @Inject
  private PersistenceManagerFactory pmf;

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    try {
      getServletContext().setAttribute("pmf", pmf);
      getServletContext().setAttribute("req", req);
      getServletContext().setAttribute("resp", resp);
      getServletContext().getRequestDispatcher("/WEB-INF/embed.jsp").forward(req, resp);

    } catch (ServletException e) {
      e.printStackTrace();
    }
  }
}