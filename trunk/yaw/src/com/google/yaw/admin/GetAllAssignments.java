package com.google.yaw.admin;

import com.google.yaw.Util;
import com.google.yaw.model.Assignment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
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
        
        try {
            JSONObject jsonRepsonse = new JSONObject();
            jsonRepsonse.put("total", 1);
            jsonRepsonse.put("page", 1);
            jsonRepsonse.put("records", assignments.size());
            
            JSONArray rows = new JSONArray();
            int count = 1;
            for (Assignment assignment : assignments) {
                String id = assignment.getId();
                String description = assignment.getDescription();
                String category = assignment.getCategory();
                String status = assignment.getStatus().toString();

                JSONObject row = new JSONObject();
                row.put("id", count++);
                
                List<String> data = new ArrayList<String>();
                data.add(id);
                data.add(description);
                data.add(category);
                data.add(status);            
                row.put("cell", data);

                rows.put(row);
            }
            jsonRepsonse.put("rows", rows);
            
            resp.setContentType("text/javascript");
            resp.getWriter().println(jsonRepsonse);
        } catch (JSONException e) {
            log.warning(e.toString());
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } finally {
            pm.close();
        }
    }
}
