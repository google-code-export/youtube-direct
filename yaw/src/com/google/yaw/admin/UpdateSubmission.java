package com.google.yaw.admin;

import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gdata.data.youtube.VideoEntry;
import com.google.gdata.util.ServiceException;
import com.google.yaw.Util;
import com.google.yaw.YouTubeApiManager;
import com.google.yaw.model.VideoSubmission;
import com.google.yaw.model.VideoSubmission.ModerationStatus;

public class UpdateSubmission extends HttpServlet {

  private static final Logger log = Logger.getLogger(UpdateSubmission.class.getName());

  @SuppressWarnings("cast")
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

    PersistenceManagerFactory pmf = Util.getPersistenceManagerFactory();
    PersistenceManager pm = pmf.getPersistenceManager();

    try {
      String json = Util.getPostBody(req);

      VideoSubmission entry = null;

      VideoSubmission jsonObj = Util.GSON.fromJson(json, VideoSubmission.class);

      String id = jsonObj.getId();

      entry = (VideoSubmission) pm.getObjectById(VideoSubmission.class, id);

      ModerationStatus currentStatus = entry.getStatus();
      ModerationStatus newStatus = jsonObj.getStatus();

      boolean hasEmail = (entry.getNotifyEmail() != null);
      boolean isRejectedOrApproved = (currentStatus !=  newStatus) && 
          (newStatus != ModerationStatus.UNREVIEWED);

      if (hasEmail && isRejectedOrApproved) {
        Util.sendNotifyEmail(entry, newStatus, entry.getNotifyEmail(), null);
      }
      
      entry.setStatus(jsonObj.getStatus());
      entry.setVideoTitle(jsonObj.getVideoTitle());
      entry.setVideoDescription(jsonObj.getVideoDescription());
      entry.setVideoTags(jsonObj.getVideoTags());
      entry.setUpdated(new Date());
      
      if (currentStatus != newStatus && newStatus == ModerationStatus.APPROVED) {
        //TODO: Move this to a properties file setting.
        String prependText = String.format("Uploaded in response to %s", entry.getArticleUrl());
        
        if (!entry.getVideoDescription().startsWith(prependText)) {
          // We only want to update the video if the text isn't already there.
          UpdateVideoDescription(entry, prependText);
        }
      }

      pm.makePersistent(entry);
      // FullTextIndexer.addIndex(entry, entry.getClass());
      // FullTextIndexer.reIndex();

      resp.setContentType("text/javascript");
      resp.getWriter().println(Util.GSON.toJson(entry));

    } catch (Exception e) {
      log.warning(e.toString());
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
    } finally {
      pm.close();
    }
  }
  
  /**
   * Updates the description of a video, both in the datastore and on YouTube, to prepend the
   * "branding" text.
   * This should be called when a video submission is marked for approval.
   * Note that the VideoSubmission is updated in-memory, but a call to
   * PersistenceManager.makePersistent(VideoSubmission) must be made by the calling code to save
   * the changes to the datastore.
   * @param videoSubmission The datastore object that is to be changed. Note that this parameter
   * will be modified by this method and must be persisted by the calling code.
   * @param prependText The text that should be prepending to the video's description.
   * @return A YouTube API VideoEntry object with the updated description, or null if the video
   * could not be updated.
   */
  private VideoEntry UpdateVideoDescription(VideoSubmission videoSubmission, String prependText) {
    String videoId = videoSubmission.getVideoId();
    log.info(String.format("Updating description of submission id '%s' (YouTube video id '%s').",
            videoSubmission.getId(), videoId));

    YouTubeApiManager apiManager = new YouTubeApiManager();
    apiManager.setToken(videoSubmission.getAuthSubToken());
    
    VideoEntry videoEntry = apiManager.getVideoEntry(videoId);
    if (videoEntry == null) {
      log.warning(String.format("Couldn't get video with id '%s' in the uploads feed of user " +
      		"'%s'. Perhaps the AuthSub token has been revoked?", videoId,
      		videoSubmission.getYouTubeName()));
    } else {
      String currentText = videoSubmission.getVideoDescription();
      String newText = String.format("%s\n\n%s", prependText, currentText);
      
      // Update the datastore entry.
      videoSubmission.setVideoDescription(newText);
      
      videoEntry.getMediaGroup().getDescription().setPlainTextContent(newText);
      try {
        // And update the YouTube.com video as well.
        videoEntry.update();
        return videoEntry;
      } catch (IOException e) {
        log.log(Level.WARNING, String.format("Error while updating video id '%s':", videoId), e);
      } catch (ServiceException e) {
        log.log(Level.WARNING, String.format("Error while updating video id '%s':", videoId), e);
      }
    }
    
    return null;
  }
}
