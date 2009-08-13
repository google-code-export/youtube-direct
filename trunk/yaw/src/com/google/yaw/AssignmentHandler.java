package com.google.yaw;

import com.google.yaw.model.Assignment;
import com.google.yaw.model.Assignment.AssignmentStatus;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AssignmentHandler extends HttpServlet {

    private static final Logger log = Logger
    .getLogger(AssignmentHandler.class.getName());
    
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        
        String description = req.getParameter("description");
        String category = req.getParameter("category");
        String assignmentStatus = req.getParameter("assignmentStatus");
        
        try {
            if (Util.isNullOrEmpty(description)) {
                throw new IllegalArgumentException("'description' parameter is null or empty.");
            }
            if (Util.isNullOrEmpty(category)) {
                throw new IllegalArgumentException("'category' parameter is null or empty.");
            }
            if (Util.isNullOrEmpty(assignmentStatus)) {
                throw new IllegalArgumentException("'assignmentStatus' parameter is null or empty.");
            }

            Assignment assignment = new Assignment(description, category,
                            AssignmentStatus.valueOf(assignmentStatus));

            Util.persistJdo(assignment);

            //TODO: Don't hardcode this.
            resp.sendRedirect("/assignments");
        } catch(IllegalArgumentException e) {
            log.finer(e.toString());
            //TODO: Don't hardcode this.
            resp.sendRedirect("/assignments?message=" + e.getMessage());
        }
    }
}
