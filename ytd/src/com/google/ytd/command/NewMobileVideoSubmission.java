package com.google.ytd.command;

import com.google.gdata.util.ServiceException;
import com.google.ytd.youtube.YouTubeApiHelper;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.inject.Inject;
import com.google.ytd.dao.AssignmentDao;
import com.google.ytd.dao.UserAuthTokenDao;
import com.google.ytd.dao.VideoSubmissionDao;
import com.google.ytd.dao.AdminConfigDao;
import com.google.ytd.model.UserAuthToken;
import com.google.ytd.model.VideoSubmission;
import com.google.ytd.util.Util;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@NonAdmin
public class NewMobileVideoSubmission extends Command {
  private static final Logger log = Logger.getLogger(NewMobileVideoSubmission.class.getName());
  private VideoSubmissionDao submissionDao = null;
  private UserAuthTokenDao userAuthTokenDao = null;
  private AssignmentDao assignmentDao = null;
  YouTubeApiHelper youTubeApiHelper;
  private boolean isTokenClientLogin = false;

  private Util util = null;

  @Inject
  public NewMobileVideoSubmission(Util util, VideoSubmissionDao submissionDao,
      AssignmentDao assignmentDao, UserAuthTokenDao userAuthTokenDao,
      AdminConfigDao adminConfigDao, YouTubeApiHelper youTubeApiHelper) {
    this.util = util;
    this.assignmentDao = assignmentDao;
    this.submissionDao = submissionDao;
    this.userAuthTokenDao = userAuthTokenDao;
    this.youTubeApiHelper = youTubeApiHelper;
  }

  @Override
  public JSONObject execute() throws JSONException {
    JSONObject json = new JSONObject();
    String assignmentId = getParam("assignmentId");
    String videoId = getParam("videoId");
    String clientLoginToken = getParam("clientLoginToken");
    String authToken = getParam("authToken");
    String youTubeEmail = getParam("youTubeName");
    String title = getParam("title");
    String description = getParam("description");
    String videoDate = getParam("videoDate");
    String videoLocation = getParam("videoLocation");
    String tags = getParam("tags");

    if (!util.isNullOrEmpty(clientLoginToken)) {
      isTokenClientLogin = true;
    }

    if (util.isNullOrEmpty(assignmentId)) {
      assignmentId = this.assignmentDao.getDefaultMobileAssignmentId() + "";
    }

    if (util.isNullOrEmpty(videoId)) {
      throw new IllegalArgumentException("Missing required param: videoId");
    }

    if (util.isNullOrEmpty(clientLoginToken) && isTokenClientLogin) {
      throw new IllegalArgumentException("Missing required param: clientLoginToken");
    }

    if (util.isNullOrEmpty(authToken) && !isTokenClientLogin) {
      throw new IllegalArgumentException("Missing required param: authToken");
    }

    if (util.isNullOrEmpty(youTubeEmail)) {
      throw new IllegalArgumentException("Missing required param: youTubeName");
    }

    if (util.isNullOrEmpty(title)) {
      throw new IllegalArgumentException("Missing required param: title");
    }

    if (util.isNullOrEmpty(description)) {
      throw new IllegalArgumentException("Missing required param: description");
    }

    if (util.isNullOrEmpty(videoDate)) {
      throw new IllegalArgumentException("Missing required param: videoDate");
    }

    // translate the Google account email into YouTube account name
    String youTubeName = null;
    if (isTokenClientLogin) {
      youTubeName = getYouTubeName(clientLoginToken, youTubeEmail);
    } else {
      youTubeName = getYouTubeName(authToken, youTubeEmail);
    }

    VideoSubmission submission = new VideoSubmission();
    submission.setAssignmentId(Long.parseLong(assignmentId));
    submission.setYouTubeName(youTubeName);
    submission.setVideoSource(VideoSubmission.VideoSource.MOBILE_SUBMIT);
    submission.setVideoId(videoId);
    submission.setVideoTitle(title);
    submission.setVideoDescription(description);
    submission.setVideoDate(videoDate);

    if (!util.isNullOrEmpty(videoLocation)) {
      submission.setVideoLocation(videoLocation);
    }

    if (!util.isNullOrEmpty(tags)) {
      submission.setVideoTags(tags);
    }

    submissionDao.save(submission);

    if (isTokenClientLogin) {
    userAuthTokenDao.setUserAuthToken(youTubeName, clientLoginToken,
        UserAuthToken.TokenType.CLIENT_LOGIN);
    } else {
      userAuthTokenDao.setUserAuthToken(youTubeName, authToken,
        UserAuthToken.TokenType.AUTH_SUB);
    }

    return json;
  }

  /**
   * The mobile app sends user account email, e.g. joe.cool@gmail.com
   * we need the associated (linked) YouTube account name in order to access the feeds later on.
   * This method resolves account email to YouTube account name   
   * @param loginToken login token supplied by the mobile app
   * @param youTubeEmail email address supplied by the mobile app
   * @return YouTube account name
   * @throws JSONException when resolution fail
   */
  private String getYouTubeName(String loginToken, String youTubeEmail) throws JSONException {
    // the mobile app sends user account email, e.g. joe.cool@gmail.com
    // we need the associated (lnked) YouTube account name in order to access the feeds later on
    String youTubeName;
    if (isTokenClientLogin) {
      youTubeApiHelper.setClientLoginToken(loginToken);
    } else {
      youTubeApiHelper.setAuthSubToken(loginToken);
    }
    try {
      log.fine(String.format("Resolving email '%s' to YT user name", youTubeEmail));
      youTubeName = youTubeApiHelper.getCurrentUsername();
    } catch (ServiceException e) {
      log.log(Level.WARNING, "Unable to resolve :" + youTubeEmail + " to YT username", e);
      throw new JSONException(e);
    } catch (IOException e) {
      log.log(Level.WARNING, "Unable to resolve :" + youTubeEmail + " to YT username", e);
      throw new JSONException(e);
    }
    return youTubeName;
  }
}
