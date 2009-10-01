package com.google.yaw.admin;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonParseException;
import com.google.yaw.Util;
import com.google.yaw.YouTubeApiManager;
import com.google.yaw.model.AdminConfig;
import com.google.yaw.model.Assignment;
import com.google.yaw.model.Assignment.AssignmentStatus;

/**
 * Servlet responsible for creating new assignment
 */
public class NewAssignment extends HttpServlet {
  private static final Logger log = Logger.getLogger(NewAssignment.class.getName());

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    PersistenceManager pm = Util.getPersistenceManagerFactory().getPersistenceManager();

    try {
      String json = Util.getPostBody(req);
      if (Util.isNullOrEmpty(json)) {
        throw new IllegalArgumentException("No JSON data found in HTTP POST request.");
      }
      
      Assignment jsonObj = Util.GSON.fromJson(json, Assignment.class);

      Assignment assignment = new Assignment();
      assignment.setStatus(jsonObj.getStatus());
      assignment.setDescription(jsonObj.getDescription());
      assignment.setCategory(jsonObj.getCategory());
      
      // Need to make it persistant first in order to get an id assigned to it.
      pm.makePersistent(assignment);
      
      if (assignment.getStatus() == AssignmentStatus.ACTIVE &&
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
          
          // Persist again with the updated playlist id.
          pm.makePersistent(assignment);
        }
      }
      
      log.info(String.format("Added assignment id %d to the datastore.", assignment.getId()));

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
