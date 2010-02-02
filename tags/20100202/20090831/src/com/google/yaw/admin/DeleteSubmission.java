package com.google.yaw.admin;

import java.io.IOException;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.yaw.Util;
import com.google.yaw.model.VideoSubmission;

public class DeleteSubmission extends HttpServlet {

  private static final Logger log = Logger.getLogger(DeleteSubmission.class.getName());

  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String id = req.getParameter("id");

    PersistenceManagerFactory pmf = Util.getPersistenceManagerFactory();
    PersistenceManager pm = pmf.getPersistenceManager();

    try {
      log.warning("delete for id = " + id);
      VideoSubmission entry = (VideoSubmission) pm.getObjectById(VideoSubmission.class, id);

      pm.deletePersistent(entry);

      resp.setContentType("text/plain");
      resp.getWriter().print("success");

    } catch (Exception e) {
      resp.setContentType("text/plain");
      resp.getWriter().print(e.getMessage());
    } finally {
      pm.close();
    }
  }
}
