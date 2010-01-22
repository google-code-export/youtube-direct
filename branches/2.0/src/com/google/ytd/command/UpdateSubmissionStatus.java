package com.google.ytd.command;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gdata.data.youtube.VideoEntry;
import com.google.gdata.data.youtube.YouTubeMediaGroup;
import com.google.gdata.util.ServiceException;
import com.google.inject.Inject;
import com.google.ytd.dao.AdminConfigDao;
import com.google.ytd.dao.AssignmentDao;
import com.google.ytd.dao.SubmissionDao;
import com.google.ytd.model.AdminConfig;
import com.google.ytd.model.Assignment;
import com.google.ytd.model.VideoSubmission;
import com.google.ytd.model.AdminConfig.BrandingModeType;
import com.google.ytd.model.VideoSubmission.ModerationStatus;
import com.google.ytd.model.VideoSubmission.VideoSource;
import com.google.ytd.util.EmailUtil;
import com.google.ytd.util.Util;
import com.google.ytd.youtube.YouTubeApiHelper;

public class UpdateSubmissionStatus extends Command {
  private static final Logger LOG = Logger.getLogger(UpdateSubmissionStatus.class.getName());

  private AssignmentDao assignmentDao = null;
  private AdminConfigDao adminConfigDao = null;
  private SubmissionDao submissionDao = null;

  @Inject
  private Util util;

  @Inject
  private EmailUtil emailUtil;

  @Inject
  private YouTubeApiHelper youTubeApiHelper;

  @Inject
  public UpdateSubmissionStatus(AssignmentDao assignmentDao, SubmissionDao submissionDao,
      AdminConfigDao adminConfigDao) {
    this.assignmentDao = assignmentDao;
    this.submissionDao = submissionDao;
    this.adminConfigDao = adminConfigDao;
  }

  @Override
  public JSONObject execute() throws JSONException {
    LOG.info(this.toString());
    JSONObject json = new JSONObject();
    String id = getParam("id");
    String status = getParam("status");

    if (util.isNullOrEmpty(id)) {
      throw new IllegalArgumentException("Missing required param: id");
    }
    if (util.isNullOrEmpty(status)) {
      throw new IllegalArgumentException("Missing required param: status");
    }

    VideoSubmission submission = submissionDao.getSubmissionById(id);

    if (submission == null) {
      throw new IllegalArgumentException("The input video id cannot be located.");
    }

    ModerationStatus newStatus = ModerationStatus.valueOf(status.toUpperCase());
    ModerationStatus currentStatus = submission.getStatus();

    if (newStatus == currentStatus) {
      return json;
    } else {

      // Set the YouTubeApiHelper with the admin auth token
      String token = adminConfigDao.getAdminConfig().getYouTubeAuthSubToken();
      if (util.isNullOrEmpty(token)) {
        throw new IllegalStateException("No AuthSub token found in admin config.");
      } else {
        youTubeApiHelper.setToken(token);
      }

      switch (newStatus) {
      case APPROVED:
        submission.setStatus(ModerationStatus.APPROVED);
        onApproved(submission);
        break;
      case REJECTED:
        submission.setStatus(ModerationStatus.REJECTED);
        onRejected(submission);
        break;
      case SPAM:
        submission.setStatus(ModerationStatus.SPAM);
        onRejected(submission);
        break;
      case UNREVIEWED:
        submission.setStatus(ModerationStatus.UNREVIEWED);
        onRejected(submission);
        break;
      }
      submission.setUpdated(new Date());
      submissionDao.save(submission);
    }

    return json;
  }

  private void onRejected(VideoSubmission submission) {
    AdminConfig adminConfig = adminConfigDao.getAdminConfig();

    // Set the YouTubeApiHelper with the admin auth token
    String token = adminConfig.getYouTubeAuthSubToken();
    if (util.isNullOrEmpty(token)) {
      LOG.warning(String.format("No AuthSub token found in admin config."));
    } else {
      youTubeApiHelper.setToken(token);
    }

    // TODO: Handle removing the branding if a video goes from APPROVED to
    // REJECTED.

    // Remove video to YouTube playlist if it is in one.
    if (submission.isInPlaylist()) {
      if (removeFromPlaylist(submission)) {
        submission.setIsInPlaylist(false);
        submission = submissionDao.save(submission);
      }
    }

    // Notify the submitter of rejection if there is a notify email
    if (adminConfig.isModerationEmail() && (submission.getNotifyEmail() != null)) {
      emailUtil.sendNotificationEmail(submission, ModerationStatus.REJECTED);
    }
  }

  private void onApproved(VideoSubmission submission) {
    AdminConfig adminConfig = adminConfigDao.getAdminConfig();

    // Turn branding on if applicable
    if (adminConfig.getBrandingMode() == BrandingModeType.ON.ordinal()) {
      String linkBackText = adminConfig.getLinkBackText();
      if (!util.isNullOrEmpty(linkBackText)) {
        String prependText = linkBackText.replace("ARTICLE_URL", submission.getArticleUrl());

        if (!submission.getVideoDescription().contains(prependText)) {
          // We only want to update the video if the text isn't already there.
          updateVideoDescription(submission, prependText, adminConfig.getDefaultTag());
        }
      }

      // Flip the moderation bit to approved for new upload
      if (submission.getVideoSource() == VideoSource.NEW_UPLOAD) {
        youTubeApiHelper.updateModeration(submission.getVideoId(), true);
      }
    }

    LOG.info(submission.isInPlaylist() + " is ...");
    // Add video to YouTube playlist if it isn't in it already.
    if (!submission.isInPlaylist()) {
      LOG.info("not in playlist");
      if (addToPlaylist(submission)) {
        LOG.info("added to playlist");
        submission.setIsInPlaylist(true);
        submission = submissionDao.save(submission);
      }
    }

    // Notify the submitter of approval if there is a notify email
    if (adminConfig.isModerationEmail() && (submission.getNotifyEmail() != null)) {
      emailUtil.sendNotificationEmail(submission, ModerationStatus.APPROVED);
    }
  }

  /**
   * Adds a video to a YouTube playlist corresponding to the video's assignment.
   * 
   * @param apiManager
   *          The YouTubeApiManager instance to handle YouTube API calls.
   * @param videoSubmission
   *          The video to add.
   * @return true if the video was added; false otherwise.
   */
  private boolean addToPlaylist(VideoSubmission videoSubmission) {
    long assignmentId = videoSubmission.getAssignmentId();
    Assignment assignment = assignmentDao.getAssignmentById(assignmentId);

    if (assignment == null) {
      LOG.warning(String.format("Couldn't find assignment id '%d' for video id '%s'.",
          assignmentId, videoSubmission.getId()));
      return false;
    }

    String playlistId = assignment.getPlaylistId();
    if (util.isNullOrEmpty(playlistId)) {
      LOG.warning(String.format("Assignment id '%d' does not have an associated playlist.",
          assignmentId));
      return false;
    }

    // TODO: A playlist can have at most 200 videos. There needs to be a way to
    // check for failures
    // due to too many videos, and prevent continuously trying to add the same
    // video to the same
    // full playlist.
    return youTubeApiHelper.insertVideoIntoPlaylist(playlistId, videoSubmission.getVideoId());
  }

  /**
   * Removes a video from a YouTube playlist corresponding to the video's
   * assignment.
   * 
   * @param apiManager
   *          The YouTubeApiManager instance to handle YouTube API calls.
   * @param videoSubmission
   *          The video to remove.
   * @return true if the video was removed; false otherwise.
   */
  private boolean removeFromPlaylist(VideoSubmission videoSubmission) {
    long assignmentId = videoSubmission.getAssignmentId();
    Assignment assignment = assignmentDao.getAssignmentById(assignmentId);

    if (assignment == null) {
      LOG.warning(String.format("Couldn't find assignment id '%d' for video id '%s'.",
          assignmentId, videoSubmission.getId()));
      return false;
    }

    String playlistId = assignment.getPlaylistId();
    if (util.isNullOrEmpty(playlistId)) {
      LOG.warning(String.format("Assignment id '%d' does not have an associated playlist.",
          assignmentId));
      return false;
    }

    return youTubeApiHelper.removeVideoFromPlaylist(playlistId, videoSubmission.getVideoId());
  }

  /**
   * Updates the description of a video, both in the datastore and on YouTube,
   * to prepend the "branding" text and apply a tag. This should be called when
   * a video submission is marked for approval. Note that the VideoSubmission is
   * updated in-memory, but a call to
   * PersistenceManager.makePersistent(VideoSubmission) must be made by the
   * calling code to save the changes to the datastore.
   * 
   * @param videoSubmission
   *          The datastore object that is to be changed. Note that this
   *          parameter will be modified by this method and must be persisted by
   *          the calling code.
   * @param prependText
   *          The text that should be prepending to the video's description.
   * @return A YouTube API VideoEntry object with the updated description, or
   *         null if the video could not be updated.
   */
  private VideoEntry updateVideoDescription(VideoSubmission videoSubmission, String prependText,
      String newTag) {
    String videoId = videoSubmission.getVideoId();
    LOG.info(String.format("Updating description and tags of id '%s' (YouTube video id '%s').",
        videoSubmission.getId(), videoId));

    VideoEntry videoEntry = youTubeApiHelper.getUploadsVideoEntry(videoId);
    if (videoEntry == null) {
      LOG.warning(String.format("Couldn't get video with id '%s' in the uploads feed of user "
          + "'%s'. Perhaps the AuthSub token has been revoked?", videoId, videoSubmission
          .getYouTubeName()));
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
        // This should work as expected even if the tag already exists; No
        // duplicates will be added.
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
        LOG.log(Level.WARNING, String.format("Error while updating video id '%s':", videoId), e);
      } catch (ServiceException e) {
        LOG.log(Level.WARNING, String.format("Error while updating video id '%s':", videoId), e);
      }
    }

    return null;
  }
}
