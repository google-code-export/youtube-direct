package com.google.yaw.admin;

import com.google.yaw.Util;
import com.google.yaw.model.Assignment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    private static final Logger log = Logger.getLogger(GetAllSubmissions.class
            .getName());

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        PersistenceManagerFactory pmf = Util.getPersistenceManagerFactory();
        PersistenceManager pm = pmf.getPersistenceManager();

        try {
            Query query = pm.newQuery(Assignment.class);
            List<Assignment> assignments = (List<Assignment>)query.execute();

            JSONArray jsonArray = new JSONArray();

            for (Assignment assignment : assignments) {
                String id = assignment.getId();
                String description = assignment.getDescription();
                String category = assignment.getCategory();
                String status = assignment.getStatus().toString();

                JSONObject jsonObj = new JSONObject();
                jsonObj.put("id", id);
                jsonObj.put("description", description);
                jsonObj.put("category", category);
                jsonObj.put("status", status);

                jsonArray.put(jsonObj);
            }
            
            resp.setContentType("text/javascript");
            resp.getWriter().println(jsonArray);
        } catch (JSONException e) {
            log.warning(e.toString());
            resp.sendError(500, e.getMessage());
        } finally {
            pm.close();
        }
    }
}
