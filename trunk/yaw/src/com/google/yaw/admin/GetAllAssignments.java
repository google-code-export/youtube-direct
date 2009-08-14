package com.google.yaw.admin;

import com.google.yaw.Util;
import com.google.yaw.model.Assignment;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GetAllAssignments extends HttpServlet {
    private static final Logger log = Logger.getLogger(GetAllAssignments.class
            .getName());

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        PersistenceManagerFactory pmf = Util.getPersistenceManagerFactory();
        PersistenceManager pm = pmf.getPersistenceManager();

        Query query = pm.newQuery(Assignment.class);
        List<Assignment> assignments = (List<Assignment>)query.execute();
        
        String json = Util.GSON.toJson(assignments);

        resp.setContentType("text/javascript");
        resp.getWriter().println(json);
        
        pm.close();
    }
}
