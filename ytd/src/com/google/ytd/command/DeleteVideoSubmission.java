package com.google.ytd.command;

import org.json.JSONObject;

import java.util.logging.Logger;

import com.google.inject.Inject;
import com.google.ytd.dao.AdminConfigDao;
import com.google.ytd.dao.AssignmentDao;
import com.google.ytd.dao.VideoSubmissionDao;
import com.google.ytd.model.Assignment;
import com.google.ytd.model.VideoSubmission;
import com.google.ytd.util.Util;
import com.google.ytd.youtube.YouTubeApiHelper;

public class DeleteVideoSubmission extends Command {
  private static final Logger LOG = Logger.getLogger(DeleteVideoSubmission.class.getName());

  @Inject
  private Util util = null;
  
  @Inject
  private YouTubeApiHelper adminYouTubeApi;
  
  private VideoSubmissionDao videoSubmissionDao = null;
  private AdminConfigDao adminConfigDao = null;
  private AssignmentDao assignmentDao = null;
  
  @Inject
  public DeleteVideoSubmission(VideoSubmissionDao videoSubmissionDao, AdminConfigDao adminConfigDao,
      AssignmentDao assignmentDao) {
    this.videoSubmissionDao = videoSubmissionDao;
    this.adminConfigDao = adminConfigDao;
    this.assignmentDao = assignmentDao;
  }

  @Override
  public JSONObject execute() {
    JSONObject json = new JSONObject();
    String id = getParam("id");

    if (util.isNullOrEmpty(id)) {
      throw new IllegalArgumentException("Missing required param: id");
    }

    VideoSubmission videoSubmission = videoSubmissionDao.getSubmissionById(id);
    if (videoSubmission.isInPlaylist()) {
      removeFromPlaylist(videoSubmission);
    }
    
    videoSubmissionDao.deleteSubmission(id);
    
    return json;
  }
  
  private void removeFromPlaylist(VideoSubmission videoSubmission) {
    String token = adminConfigDao.getAdminConfig().getYouTubeAuthSubToken();
    if (util.isNullOrEmpty(token)) {
      LOG.warning("No AuthSub token found in admin config.");
      return;
    }

    adminYouTubeApi.setAuthSubToken(token);

    long assignmentId = videoSubmission.getAssignmentId();
    Assignment assignment = assignmentDao.getAssignmentById(assignmentId);

    if (assignment == null) {
      LOG.warning(String.format("Couldn't find assignment id '%d' for video id '%s'.", assignmentId,
              videoSubmission.getId()));
      return;
    }

    String playlistId = assignment.getPlaylistId();
    if (util.isNullOrEmpty(playlistId)) {
      LOG.warning(String.format("Assignment id '%d' does not have an associated playlist.",
          assignmentId));
      return;
    }

    adminYouTubeApi.removeVideoFromPlaylist(playlistId, videoSubmission.getVideoId());
  }
}
