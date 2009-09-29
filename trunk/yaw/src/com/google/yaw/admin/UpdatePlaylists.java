package com.google.yaw.admin;

import com.google.gdata.util.AuthenticationException;
import com.google.yaw.Util;
import com.google.yaw.YouTubeApiManager;
import com.google.yaw.model.AdminConfig;
import com.google.yaw.model.Assignment;
import com.google.yaw.model.VideoSubmission;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet that automatically creates and updates playlists for each assignment's approved videos.
 */
public class UpdatePlaylists extends HttpServlet {
  private static final Logger log = Logger.getLogger(UpdatePlaylists.class.getName());

  @SuppressWarnings("unchecked")
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    PersistenceManager pm = Util.getPersistenceManagerFactory().getPersistenceManager();
    
    // The total number of videos processed in this invocation.
    int count = 0;
    // The total number of approved videos that are not yet in a playlist.
    int total = 0;

    try {
      log.info("Starting update playlist request...");
      
      AdminConfig adminConfig = Util.getAdminConfig();
      YouTubeApiManager apiManager = new YouTubeApiManager();
      
      String token = adminConfig.getYouTubeAuthSubToken();
      if (Util.isNullOrEmpty(token)) {
        throw new IllegalArgumentException("No YouTube AtuhSub token specified in configuration.");
      }
      apiManager.setToken(token);
      
      Query query = pm.newQuery(VideoSubmission.class);
      //TODO: Don't hardcode 1 as the approved status; use the enum instead.
      query.setFilter("isInPlaylist == false && status == 1");
      query.setOrdering("created asc");

      List<VideoSubmission> videoSubmissions = (List<VideoSubmission>) query.execute();
      total = videoSubmissions.size();
      log.info(String.format("Found %d approved videos that are not yet in playlists.", total));
      
      for (VideoSubmission videoSubmission : videoSubmissions) {
        if(addToPlaylist(apiManager, videoSubmission)) {
          videoSubmission.setIsInPlaylist(true);
          pm.makePersistent(videoSubmission);
          count++;
        }
      }
    } catch (IllegalArgumentException e) {
      log.log(Level.WARNING, "", e);
    } finally {
      pm.close();
      
      String message = String.format("Ending update playlist request. Successfully added %d of " +
      		"%d videos to playlists.", count, total);
      log.info(message);
      resp.setContentType("text/plain");
      resp.getWriter().println(message);
    }
  }
  
  private boolean addToPlaylist(YouTubeApiManager apiManager, VideoSubmission videoSubmission) {
    long assignmentId = videoSubmission.getAssignmentId();
    Assignment assignment = Util.getAssignmentById(assignmentId);
    
    if (assignment == null) {
      log.warning(String.format("Couldn't find assignment id '%d' for video id '%s'.", assignmentId,
              videoSubmission.getId()));
      
      return false;
    } else {
      String playlistId = assignment.getPlaylistId();
      if (Util.isNullOrEmpty(playlistId)) {
        playlistId = apiManager.createPlaylist(String.format("Playlist for Assignment #%d",
                assignmentId), assignment.getDescription());
        
        if (playlistId == null) {
          log.warning("Couldn't create new playlist, so can't add video to it.");
          return false;
        }
        
        assignment.setPlaylistId(playlistId);
        Util.persistJdo(assignment);
      }
      
      //TODO: A playlist can have at most 200 videos. There needs to be a way to check for failures
      // due to too many videos, and prevent continuously trying to add the same video to the same
      // full playlist.
      return apiManager.insertVideoIntoPlaylist(playlistId, videoSubmission.getVideoId());
    }
  }
}