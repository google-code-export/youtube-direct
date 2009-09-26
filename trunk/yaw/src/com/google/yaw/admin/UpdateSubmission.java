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
import com.google.yaw.Util;
import com.google.yaw.YouTubeApiManager;
import com.google.yaw.model.AdminConfig;
import com.google.yaw.model.VideoSubmission;
import com.google.yaw.model.AdminConfig.BrandingModeType;
import com.google.yaw.model.VideoSubmission.ModerationStatus;
import com.google.yaw.model.VideoSubmission.VideoSource;

/**
 * Servlet responsible for updating submissions, both in the AppEngine datastore and on YouTube.
 */
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

      boolean hasEmail = !Util.isNullOrEmpty(entry.getNotifyEmail());
      
      AdminConfig adminConfig = Util.getAdminConfig();
      
      boolean isRejectedOrApproved = (currentStatus !=  newStatus) && 
          (newStatus != ModerationStatus.UNREVIEWED || newStatus != ModerationStatus.SPAM);

      if (adminConfig.isModerationEmail() && hasEmail && isRejectedOrApproved
              && currentStatus != newStatus) {
        Util.sendNotificationEmail(entry, newStatus);
      }
      
      entry.setStatus(jsonObj.getStatus());
      entry.setVideoTitle(jsonObj.getVideoTitle());
      entry.setVideoDescription(jsonObj.getVideoDescription());
      entry.setVideoTags(jsonObj.getVideoTags());
      entry.setUpdated(new Date());
      
      //TODO: Handle removing the branding if a video goes from APPROVED to REJECTED.
      if (adminConfig.getBrandingMode() == BrandingModeType.ON.ordinal() &&
              currentStatus != newStatus && newStatus == ModerationStatus.APPROVED) {

        String prependText = adminConfig.getLinkBackText().replace(
                "ARTICLE_URL", entry.getArticleUrl());
        
        if (!entry.getVideoDescription().contains(prependText)) {
          // We only want to update the video if the text isn't already there.
          updateVideoDescription(entry, prependText, adminConfig.getDefaultTag());
        }
      }
      
      // We can only update moderation for videos that were uploaded with our developer key.
      if (entry.getVideoSource() == VideoSource.NEW_UPLOAD &&
              adminConfig.getBrandingMode() == BrandingModeType.ON.ordinal()) {
        // Create a new API manager in this step because swapping out credentials in the same
        // instance doesn't work, and we need to use the credentials of the account that owns the
        // developer token used to upload the video.
        YouTubeApiManager apiManager = new YouTubeApiManager();
        // Modifying moderation also requires a ClientLogin token retrieved from a different,
        // non-YouTube specific, ClientLogin endpoint. This call overrides that default.
        apiManager.useGoogleAccountAuthService();
        try {
          apiManager.setLoginInfo(adminConfig.getYouTubeUsername(),
                  adminConfig.getYouTubePassword());
          apiManager.updateModeration(entry.getVideoId(), newStatus == ModerationStatus.APPROVED);
        } catch (AuthenticationException e) {
          log.log(Level.WARNING, "", e);
        }
      }

      pm.makePersistent(entry);
      // FullTextIndexer.addIndex(entry, entry.getClass());
      // FullTextIndexer.reIndex();

      resp.setContentType("text/javascript");
      resp.getWriter().println(Util.GSON.toJson(entry));
    } finally {
      pm.close();
    }
  }
  
  /**
   * Updates the description of a video, both in the datastore and on YouTube, to prepend the
   * "branding" text and apply a tag.
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
  private VideoEntry updateVideoDescription(VideoSubmission videoSubmission, String prependText,
          String newTag) {
    String videoId = videoSubmission.getVideoId();
    log.info(String.format("Updating description and tags of id '%s' (YouTube video id '%s').",
            videoSubmission.getId(), videoId));

    YouTubeApiManager apiManager = new YouTubeApiManager();
    apiManager.setToken(videoSubmission.getAuthSubToken());
    
    VideoEntry videoEntry = apiManager.getUploadsVideoEntry(videoId);
    if (videoEntry == null) {
      log.warning(String.format("Couldn't get video with id '%s' in the uploads feed of user " +
      		"'%s'. Perhaps the AuthSub token has been revoked?", videoId,
      		videoSubmission.getYouTubeName()));
    } else {
      String currentDescription = videoSubmission.getVideoDescription();
      String newDescription = String.format("%s\n\n%s", prependText, currentDescription);
      
      // If we have a new tag to add, add to the datastore and YouTube entries.
      if (!Util.isNullOrEmpty(newTag)) {
        String currentTags = videoSubmission.getVideoTags();
        String[] tagsArray = currentTags.split(",\\s?");
        ArrayList<String> tagsArrayList = new ArrayList<String>(Arrays.asList(tagsArray));
        if (!tagsArrayList.contains(newTag)) {
          tagsArrayList.add(newTag);
          String newTags = Util.sortedJoin(tagsArrayList, ",");
          videoSubmission.setVideoTags(newTags);
        }
        
        YouTubeMediaGroup mg = videoEntry.getOrCreateMediaGroup();
        // This should work as expected even if the tag already exists; No duplicates will be added.
        mg.getKeywords().addKeyword(newTag);
      }
      
      // Update the datastore entry's description.
      videoSubmission.setVideoDescription(newDescription);
      
      // Update the YouTube entry's description.
      videoEntry.getMediaGroup().getDescription().setPlainTextContent(newDescription);
      
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
