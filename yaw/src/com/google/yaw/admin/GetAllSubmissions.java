package com.google.yaw.admin;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.yaw.Util;
import com.google.yaw.model.VideoSubmission;

public class GetAllSubmissions extends HttpServlet {

  private static final Logger log = Logger.getLogger(GetAllSubmissions.class.getName());

  @SuppressWarnings("unchecked")
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

    String approvedOnly = req.getParameter("approved_only");
    
    if (approvedOnly == null) {      
      approvedOnly = "0";
    }
     
    PersistenceManagerFactory pmf = Util.getPersistenceManagerFactory();
    PersistenceManager pm = pmf.getPersistenceManager();

    try {
      Query query = pm.newQuery(VideoSubmission.class);
      if (approvedOnly.equals("1")) {
        String filters = "status == 1"; 
        query.setFilter(filters);
      }
      
      query.declareImports("import java.util.Date");
      query.setOrdering("created desc");
      
      List<VideoSubmission> videoEntries = (List<VideoSubmission>) query.execute();

      String json = Util.GSON.toJson(videoEntries);

      resp.setContentType("text/javascript");
      resp.getWriter().println(json);
    } finally {
      pm.close();
    }
  }
}
