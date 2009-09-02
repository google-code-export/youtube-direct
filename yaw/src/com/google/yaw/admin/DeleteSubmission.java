package com.google.yaw.admin;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreFailureException;
import com.google.appengine.api.datastore.DatastoreTimeoutException;
import com.google.yaw.Util;
import com.google.yaw.model.VideoSubmission;

public class DeleteSubmission extends HttpServlet {

  private static final Logger log = Logger.getLogger(DeleteSubmission.class.getName());

  @Override
  @SuppressWarnings("cast")
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    PersistenceManagerFactory pmf = Util.getPersistenceManagerFactory();
    PersistenceManager pm = pmf.getPersistenceManager();

    try {
      String id = req.getParameter("id");
      if (Util.isNullOrEmpty(id)) {
        throw new IllegalArgumentException("'id' parameter is null or empty.");
      }

      log.info(String.format("Deleting VideoSubmission with id '%s'.", id));
      VideoSubmission entry = (VideoSubmission) pm.getObjectById(VideoSubmission.class, id);
      pm.deletePersistent(entry);

      resp.setContentType("text/plain");
      resp.getWriter().print(String.format("Deleted VideoSubmission with id '%s'", id));
    } catch (IllegalArgumentException e) {
      log.log(Level.WARNING, "", e);
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
    } finally {
      pm.close();
    }
  }
}
