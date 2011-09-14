package com.google.ytd.command;

import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.inject.Inject;
import com.google.ytd.dao.AdminConfigDao;
import com.google.ytd.dao.AssignmentDao;
import com.google.ytd.dao.VideoSubmissionDao;
import com.google.ytd.model.AdminConfig;
import com.google.ytd.model.Assignment;
import com.google.ytd.model.VideoSubmission;
import com.google.ytd.util.Util;
import com.google.ytd.youtube.YouTubeApiHelper;

public class UpdateVideoSubmissionAssignment extends Command {
  private static final Logger LOG = Logger.getLogger(UpdateVideoSubmissionAssignment.class.getName());

  private AssignmentDao assignmentDao = null;
  private VideoSubmissionDao submissionDao = null;
  private AdminConfigDao adminConfigDao = null;

  @Inject
  private Util util;

  @Inject
  private YouTubeApiHelper adminYouTubeApi;

  @Inject
  public UpdateVideoSubmissionAssignment(AssignmentDao assignmentDao,
      VideoSubmissionDao submissionDao, AdminConfigDao adminConfigDao) {
    this.assignmentDao = assignmentDao;
    this.submissionDao = submissionDao;
    this.adminConfigDao = adminConfigDao;
  }

  @Override
  public JSONObject execute() throws JSONException {
    JSONObject json = new JSONObject();
    String id = getParam("id");
    String oldAssignment = getParam("oldAssignment");
    String newAssignment = getParam("newAssignment");
    
    if (util.isNullOrEmpty(id)) {
      throw new IllegalArgumentException("Missing required param: id");
    }
    
    if (util.isNullOrEmpty(oldAssignment)) {
      throw new IllegalArgumentException("Missing required param: oldAssignment");
    }
    
    if (util.isNullOrEmpty(newAssignment)) {
      throw new IllegalArgumentException("Missing required param: newAssignment");
    }
    
    if (oldAssignment.equals(newAssignment)) {
      throw new IllegalArgumentException("oldAssignment and newAssignment can't be the same.");
    }

    VideoSubmission submission = submissionDao.getSubmissionById(id);
    if (submission == null) {
      throw new IllegalArgumentException("Can't find submission with id " + id);
    }
    
    if (submission.isInPlaylist()) {
      AdminConfig adminConfig = adminConfigDao.getAdminConfig();

      // Set the YouTubeApiHelper with the admin auth token
      String token = adminConfig.getYouTubeAuthSubToken();
      if (util.isNullOrEmpty(token)) {
        LOG.warning(String.format("No AuthSub token found in admin config."));
      } else {
        adminYouTubeApi.setAuthSubToken(token);
      }
      
      removeFromPlaylist(submission);
    }
    
    submission.setAssignmentId(Long.parseLong(newAssignment));
    submission = submissionDao.save(submission);
    
    boolean success = true;
    if (submission.isInPlaylist()) {
      success = addToPlaylist(submission);
    }
    
    json.put("success", success);
    return json;
  }

  private boolean addToPlaylist(VideoSubmission videoSubmission) {
    long assignmentId = videoSubmission.getAssignmentId();
    Assignment assignment = assignmentDao.getAssignmentById(assignmentId);

    if (assignment == null) {
      LOG.warning(String.format("Couldn't find assignment id '%d' for video id '%s'.", assignmentId,
          videoSubmission.getId()));
      return false;
    }

    String playlistId = assignment.getPlaylistId();
    if (util.isNullOrEmpty(playlistId)) {
      LOG.warning(String.format("Assignment id '%d' does not have an associated playlist.",
        assignmentId));
      return false;
    }

    return adminYouTubeApi.insertVideoIntoPlaylist(playlistId, videoSubmission.getVideoId());
  }

  /**
   * Removes a video from a YouTube playlist corresponding to the video's
   * assignment.
   * 
   * @param videoSubmission
   *          The video to remove.
   * @return true if the video was removed; false otherwise.
   */
  private boolean removeFromPlaylist(VideoSubmission videoSubmission) {
    long assignmentId = videoSubmission.getAssignmentId();
    Assignment assignment = assignmentDao.getAssignmentById(assignmentId);

    if (assignment == null) {
      LOG.warning(String.format("Couldn't find assignment id '%d' for video id '%s'.", assignmentId, 
          videoSubmission.getId()));
      return false;
    }

    String playlistId = assignment.getPlaylistId();
    if (util.isNullOrEmpty(playlistId)) {
      LOG.warning(String.format("Assignment id '%d' does not have an associated playlist.",
          assignmentId));
      return false;
    }

    return adminYouTubeApi.removeVideoFromPlaylist(playlistId, videoSubmission.getVideoId());
  }
}
