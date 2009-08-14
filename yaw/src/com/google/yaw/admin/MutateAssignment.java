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
import com.google.yaw.model.Assignment;

public class MutateAssignment extends HttpServlet {

    private static final Logger log = Logger.getLogger(UpdateSubmission.class
                    .getName());

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
    throws IOException {
        
        PersistenceManagerFactory pmf = Util.getPersistenceManagerFactory();
        PersistenceManager pm = pmf.getPersistenceManager();
        
        String json = Util.getPostBody(req);

        log.warning(json);

        Assignment incomingAssignment = Util.GSON.fromJson(json, Assignment.class);                      
        String id = incomingAssignment.getId();

        String filters = "id == id_";
        Query query = pm.newQuery(Assignment.class, filters);
        query.declareParameters("String id_");

        List<Assignment> list = (List<Assignment>)query.executeWithArray(
                        new Object[] { id });

        if (list.size() > 0) {
            Assignment updatedAssignment = list.get(0);
            updatedAssignment = pm.detachCopy(updatedAssignment);

            updatedAssignment.setDescription(incomingAssignment.getDescription());
            updatedAssignment.setCategory(incomingAssignment.getCategory());
            updatedAssignment.setStatus(incomingAssignment.getStatus());

            Util.persistJdo(updatedAssignment);            

            resp.setContentType("text/javascript");
            resp.getWriter().println(Util.GSON.toJson(updatedAssignment));
        } else {
            String error = String.format("Could not find Assignment with id '%s'.",
                            incomingAssignment.getId());
            log.warning(error);
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, error);
        }
        
        pm.close();
    }
}
