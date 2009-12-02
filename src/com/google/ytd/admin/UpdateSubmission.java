/* Copyright (c) 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.ytd.admin;

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
import com.google.gdata.util.ServiceException;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.ytd.YouTubeApiManager;
import com.google.ytd.dao.UserAuthTokenDao;
import com.google.ytd.model.AdminConfig;
import com.google.ytd.model.Assignment;
import com.google.ytd.model.VideoSubmission;
import com.google.ytd.model.AdminConfig.BrandingModeType;
import com.google.ytd.model.VideoSubmission.ModerationStatus;
import com.google.ytd.model.VideoSubmission.VideoSource;
import com.google.ytd.util.Util;

/**
 * Servlet responsible for updating submissions, both in the AppEngine datastore and on YouTube.
 */
@Singleton
public class UpdateSubmission extends HttpServlet {
  private static final Logger log = Logger.getLogger(UpdateSubmission.class.getName());
  @Inject
  private Util util;
  @Inject
  private PersistenceManagerFactory pmf;
  @Inject
  private YouTubeApiManager adminApiManager;
  @Inject
  private Injector injector;
  @Inject
  private UserAuthTokenDao userAuthTokenDao;

  @SuppressWarnings("cast")
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    PersistenceManager pm = pmf.getPersistenceManager();

    try {
      String json = util.getPostBody(req);

      VideoSubmission entry = null;

      VideoSubmission incomingEntry = util.GSON.fromJson(json, VideoSubmission.class);

      String id = incomingEntry.getId();

      entry = (VideoSubmission) pm.getObjectById(VideoSubmission.class, id);

      ModerationStatus currentStatus = entry.getStatus();
      ModerationStatus newStatus = incomingEntry.getStatus();

      boolean hasEmail = !util.isNullOrEmpty(entry.getNotifyEmail());

      AdminConfig adminConfig = util.getAdminConfig();

      boolean isRejectedOrApproved = (currentStatus !=  newStatus) &&
          (newStatus != ModerationStatus.UNREVIEWED || newStatus != ModerationStatus.SPAM);

      if (adminConfig.isModerationEmail() && hasEmail && isRejectedOrApproved
              && currentStatus != newStatus) {
        util.sendNotificationEmail(entry, newStatus);
      }

      //Mutates all the entry attributes with the incoming entry attributes
      entry.setStatus(incomingEntry.getStatus());
      entry.setAdminNotes(incomingEntry.getAdminNotes());
      entry.setUpdated(new Date());

      //TODO: Handle removing the branding if a video goes from APPROVED to REJECTED.
      if (adminConfig.getBrandingMode() == BrandingModeType.ON.ordinal() &&
              currentStatus != newStatus && newStatus == ModerationStatus.APPROVED) {

        String linkBackText = adminConfig.getLinkBackText();
        if (!util.isNullOrEmpty(linkBackText)) {
          String prependText = linkBackText.replace("ARTICLE_URL", entry.getArticleUrl());

          if (!entry.getVideoDescription().contains(prependText)) {
            // We only want to update the video if the text isn't already there.
            updateVideoDescription(entry, prependText, adminConfig.getDefaultTag());
          }
        }
      }

      String token = adminConfig.getYouTubeAuthSubToken();
      if (util.isNullOrEmpty(token)) {
        log.warning(String.format("No AuthSub token found in admin config."));
      } else {
        adminApiManager.setToken(token);
      }

      // We can only update moderation for videos that were uploaded with our developer key.
      if (currentStatus != newStatus && entry.getVideoSource() == VideoSource.NEW_UPLOAD &&
              adminConfig.getBrandingMode() == BrandingModeType.ON.ordinal()) {
          adminApiManager.updateModeration(entry.getVideoId(),
                  newStatus == ModerationStatus.APPROVED);
      }

      // Initiate playlist update only if there is a change in moderation status
      if (currentStatus != newStatus) {
        if (newStatus == ModerationStatus.APPROVED && !entry.isInPlaylist()) {
          // If this video is approved and it's not yet in a playlist, add it.
          if(addToPlaylist(adminApiManager, entry)) {
            entry.setIsInPlaylist(true);
          }
        } else if (newStatus != ModerationStatus.APPROVED && entry.isInPlaylist()) {
          // If this video is not approved but it's in a playlist, remove it.
          if(removeFromPlaylist(adminApiManager, entry)) {
            entry.setIsInPlaylist(false);
          }
        }
      }
      pm.makePersistent(entry);
      // FullTextIndexer.addIndex(entry, entry.getClass());
      // FullTextIndexer.reIndex();

      resp.setContentType("text/javascript");
      resp.getWriter().println(util.GSON.toJson(entry));
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

    YouTubeApiManager apiManager = injector.getInstance(YouTubeApiManager.class);

    apiManager.setToken(
        userAuthTokenDao.getUserAuthToken(videoSubmission.getYouTubeName()).getAuthSubToken());

    VideoEntry videoEntry = apiManager.getUploadsVideoEntry(videoId);
    if (videoEntry == null) {
      log.warning(String.format("Couldn't get video with id '%s' in the uploads feed of user " +
      		"'%s'. Perhaps the AuthSub token has been revoked?", videoId,
      		videoSubmission.getYouTubeName()));
    } else {
      String currentDescription = videoSubmission.getVideoDescription();
      String newDescription = String.format("%s\n\n%s", prependText, currentDescription);

      // If we have a new tag to add, add to the datastore and YouTube entries.
      if (!util.isNullOrEmpty(newTag)) {
        String currentTags = videoSubmission.getVideoTags();
        String[] tagsArray = currentTags.split(",\\s?");
        ArrayList<String> tagsArrayList = new ArrayList<String>(Arrays.asList(tagsArray));
        if (!tagsArrayList.contains(newTag)) {
          tagsArrayList.add(newTag);
          String newTags = util.sortedJoin(tagsArrayList, ",");
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

  /**
   * Adds a video to a YouTube playlist corresponding to the video's assignment.
   *
   * @param apiManager The YouTubeApiManager instance to handle YouTube API calls.
   * @param videoSubmission The video to add.
   * @return true if the video was added; false otherwise.
   */
  private boolean addToPlaylist(YouTubeApiManager apiManager, VideoSubmission videoSubmission) {
    long assignmentId = videoSubmission.getAssignmentId();
    Assignment assignment = util.getAssignmentById(assignmentId);

    if (assignment == null) {
      log.warning(String.format("Couldn't find assignment id '%d' for video id '%s'.", assignmentId,
              videoSubmission.getId()));
      return false;
    }

    String playlistId = assignment.getPlaylistId();
    if (util.isNullOrEmpty(playlistId)) {
      log.warning(String.format("Assignment id '%d' does not have an associated playlist.",
              assignmentId));
      return false;
    }

    //TODO: A playlist can have at most 200 videos. There needs to be a way to check for failures
    // due to too many videos, and prevent continuously trying to add the same video to the same
    // full playlist.
    return apiManager.insertVideoIntoPlaylist(playlistId, videoSubmission.getVideoId());
  }

  /**
   * Removes a video from a YouTube playlist corresponding to the video's assignment.
   *
   * @param apiManager The YouTubeApiManager instance to handle YouTube API calls.
   * @param videoSubmission The video to remove.
   * @return true if the video was removed; false otherwise.
   */
  private boolean removeFromPlaylist(YouTubeApiManager apiManager,
          VideoSubmission videoSubmission) {
    long assignmentId = videoSubmission.getAssignmentId();
    Assignment assignment = util.getAssignmentById(assignmentId);

    if (assignment == null) {
      log.warning(String.format("Couldn't find assignment id '%d' for video id '%s'.", assignmentId,
              videoSubmission.getId()));
      return false;
    }

    String playlistId = assignment.getPlaylistId();
    if (util.isNullOrEmpty(playlistId)) {
      log.warning(String.format("Assignment id '%d' does not have an associated playlist.",
              assignmentId));
      return false;
    }

    return apiManager.removeVideoFromPlaylist(playlistId, videoSubmission.getVideoId());
  }
}
