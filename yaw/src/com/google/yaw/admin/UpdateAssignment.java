package com.google.yaw.admin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gdata.data.youtube.VideoEntry;
import com.google.gdata.data.youtube.YouTubeMediaGroup;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;
import com.google.gson.JsonParseException;
import com.google.yaw.Util;
import com.google.yaw.YouTubeApiManager;
import com.google.yaw.model.AdminConfig;
import com.google.yaw.model.Assignment;
import com.google.yaw.model.VideoSubmission;
import com.google.yaw.model.AdminConfig.BrandingModeType;
import com.google.yaw.model.Assignment.AssignmentStatus;
import com.google.yaw.model.VideoSubmission.ModerationStatus;
import com.google.yaw.model.VideoSubmission.VideoSource;

/**
 * Servlet responsible for updating assignment
 */
public class UpdateAssignment extends HttpServlet {
  private static final Logger log = Logger.getLogger(UpdateAssignment.class.getName());

  @SuppressWarnings("cast")
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    PersistenceManager pm = Util.getPersistenceManagerFactory().getPersistenceManager();

    try {
      String json = Util.getPostBody(req);
      if (Util.isNullOrEmpty(json)) {
        throw new IllegalArgumentException("No JSON data found in HTTP POST request.");
      }

      Assignment incomingEntry = Util.GSON.fromJson(json, Assignment.class);
      long id = incomingEntry.getId();
      Assignment assignment = (Assignment) pm.getObjectById(Assignment.class, id);
      
      // If the updated assignment's status is ACTIVE and the existing assignment doesn't already
      // have a playlist, create one.
      if (incomingEntry.getStatus() == AssignmentStatus.ACTIVE &&
              Util.isNullOrEmpty(assignment.getPlaylistId())) {
        YouTubeApiManager apiManager = new YouTubeApiManager();
        
        AdminConfig adminConfig = Util.getAdminConfig();
        String token = adminConfig.getYouTubeAuthSubToken();
        if (Util.isNullOrEmpty(token)) {
          log.warning(String.format("Could not create new playlist for assignment '%s' because no" +
              " YouTube AuthSub token was found in the config.", assignment.getDescription()));
        } else {
          apiManager.setToken(token);
          String playlistId = apiManager.createPlaylist(String.format("Playlist for Assignment #%d",
                  assignment.getId()), assignment.getDescription());
          
          assignment.setPlaylistId(playlistId);
        }
      }
      
      assignment.setStatus(incomingEntry.getStatus());
      assignment.setDescription(incomingEntry.getDescription());
      assignment.setCategory(incomingEntry.getCategory());
      
      pm.makePersistent(assignment);
      
      log.info(String.format("Updated assignment id %d in the datastore.", assignment.getId()));

      resp.setContentType("text/javascript");
      resp.getWriter().println(Util.GSON.toJson(assignment));
    } catch (JsonParseException e) {
      log.log(Level.WARNING, "", e);
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
    } catch (IllegalArgumentException e) {
      log.log(Level.WARNING, "", e);
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
    } finally {
      pm.close();
    }
  }
  
}
