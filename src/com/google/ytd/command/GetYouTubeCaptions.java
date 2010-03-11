package com.google.ytd.command;

import com.google.inject.Inject;
import com.google.ytd.dao.UserAuthTokenDao;
import com.google.ytd.dao.VideoSubmissionDao;
import com.google.ytd.model.VideoSubmission;
import com.google.ytd.util.Util;
import com.google.ytd.youtube.YouTubeApiHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class GetYouTubeCaptions extends Command {
  private YouTubeApiHelper apiManager = null;
  private VideoSubmissionDao submissionDao = null;
  private UserAuthTokenDao authTokenDao = null;
  
  @Inject
  private Util util;
  
  @Inject
  public GetYouTubeCaptions(VideoSubmissionDao submissionDao, UserAuthTokenDao authTokenDao,
      YouTubeApiHelper apiManager) {
    this.submissionDao = submissionDao;
    this.authTokenDao = authTokenDao;
    this.apiManager = apiManager;
  }
  
  @Override
  public JSONObject execute() throws JSONException {
    JSONObject json = new JSONObject();
    
    String submissionId = getParam("submissionId");
    if (util.isNullOrEmpty(submissionId)) {
      throw new IllegalArgumentException("Required parameter 'submissionId' is null or empty.");
    }
    
    VideoSubmission videoSubmission = submissionDao.getSubmissionById(submissionId);
    if (videoSubmission == null) {
      throw new IllegalArgumentException(String.format("Couldn't retrieve VideoSubmission with" +
            " id '%s' from the datastore.", submissionId));
    }
    
    String username = videoSubmission.getYouTubeName();
    String authSubToken = authTokenDao.getUserAuthToken(username).getAuthSubToken();
    apiManager.setToken(authSubToken);
    
    Map<String, String> languageToUrl = apiManager.getCaptions(videoSubmission.getVideoId());
    if (languageToUrl != null) {
      json.put("captions", languageToUrl);
      json.put("authSubToken", authSubToken);
    } else {
      throw new IllegalArgumentException("Unable to retrieve caption information for the video.");
    }
    
    return json;
  }
}
