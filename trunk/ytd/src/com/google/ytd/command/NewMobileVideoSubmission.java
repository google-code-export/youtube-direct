package com.google.ytd.command;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.inject.Inject;
import com.google.ytd.dao.UserAuthTokenDao;
import com.google.ytd.dao.VideoSubmissionDao;
import com.google.ytd.model.UserAuthToken;
import com.google.ytd.model.VideoSubmission;
import com.google.ytd.util.Util;

@NonAdmin
public class NewMobileVideoSubmission extends Command {
  private VideoSubmissionDao submissionDao = null;
  private UserAuthTokenDao userAuthTokenDao = null;
  private Util util = null;

  @Inject
  public NewMobileVideoSubmission(Util util, VideoSubmissionDao submissionDao, 
      UserAuthTokenDao userAuthTokenDao) {
    this.util = util;
    this.submissionDao = submissionDao;
    this.userAuthTokenDao = userAuthTokenDao;
  }

  @Override
  public JSONObject execute() throws JSONException {
    JSONObject json = new JSONObject();
    String assignmentId = getParam("assignmentId");
    String videoId = getParam("videoId");
    String clientLoginToken = getParam("clientLoginToken");
    String youTubeName = getParam("youTubeName");
    String title = getParam("title");
    String description = getParam("description");
    String videoDate = getParam("videoDate");
    
    if (util.isNullOrEmpty(assignmentId)) {
      throw new IllegalArgumentException("Missing required param: assignmentId");
    }

    if (util.isNullOrEmpty(videoId)) {
      throw new IllegalArgumentException("Missing required param: videoId");
    }

    if (util.isNullOrEmpty(clientLoginToken)) {
      throw new IllegalArgumentException("Missing required param: clientLoginToken");
    }

    if (util.isNullOrEmpty(youTubeName)) {
      throw new IllegalArgumentException("Missing required param: youTubeName");
    }
    
    if (util.isNullOrEmpty(title)) {
      throw new IllegalArgumentException("Missing required param: title");
    }
    
    if (util.isNullOrEmpty(description)) {
      throw new IllegalArgumentException("Missing required param: description");
    }    
    
    if (util.isNullOrEmpty(videoDate)) {
      throw new IllegalArgumentException("Missing required param: description");
    }        
    
    VideoSubmission submission = new VideoSubmission();
    submission.setAssignmentId(Long.parseLong(assignmentId));
    submission.setYouTubeName(youTubeName);
    submission.setVideoSource(VideoSubmission.VideoSource.MOBILE_SUBMIT);
    submission.setVideoId(videoId);
    submission.setVideoTitle(title);
    submission.setVideoDescription(description);
    submission.setVideoDate(videoDate);
    submissionDao.save(submission);
    
    userAuthTokenDao.setUserAuthToken(youTubeName, clientLoginToken, 
        UserAuthToken.TokenType.CLIENT_LOGIN);
    
    return json;
  }
}
