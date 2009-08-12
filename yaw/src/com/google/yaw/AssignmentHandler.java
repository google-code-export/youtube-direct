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
    .getLogger(UploadResponseHandler.class.getName());
    
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        
        String description = req.getParameter("description");
        String category = req.getParameter("category");
        String assignmentStatus = req.getParameter("assignmentStatus");
        
        if (Util.isNullOrEmpty(description)) {
            //TODO: Handle bad parameters gracefully.
            log.warning("'description' parameter is null or empty.");
        }

        if (Util.isNullOrEmpty(category)) {
            //TODO: Handle bad parameters gracefully.
            log.warning("'category' parameter is null or empty.");
        }
        
        if (Util.isNullOrEmpty(assignmentStatus)) {
            //TODO: Handle bad parameters gracefully.
            log.warning("'assignmentStatus' parameter is null or empty.");
        }
        
        Assignment assignment = new Assignment(description, category,
                AssignmentStatus.valueOf(assignmentStatus));
        
        Util.persistJdo(assignment);
        
        //TODO: Do something more interesting here.
        resp.sendRedirect("/assignments.jsp");
    }
}
