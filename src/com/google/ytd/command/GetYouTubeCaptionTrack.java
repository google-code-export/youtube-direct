package com.google.ytd.command;

import com.google.inject.Inject;
import com.google.ytd.dao.UserAuthTokenDao;
import com.google.ytd.dao.VideoSubmissionDao;
import com.google.ytd.model.UserAuthToken;
import com.google.ytd.util.Util;
import com.google.ytd.youtube.YouTubeApiHelper;

import org.json.JSONException;
import org.json.JSONObject;

public class GetYouTubeCaptionTrack extends Command {
  private YouTubeApiHelper apiManager = null;
  private UserAuthTokenDao authTokenDao = null;

  @Inject
  private Util util;


  @Inject
  public GetYouTubeCaptionTrack(UserAuthTokenDao authTokenDao, YouTubeApiHelper apiManager) {
    this.authTokenDao = authTokenDao;
    this.apiManager = apiManager;
  }

  @Override
  public JSONObject execute() throws JSONException {
    JSONObject json = new JSONObject();

    String url = getParam("url");
    if (util.isNullOrEmpty(url)) {
      throw new IllegalArgumentException("Required parameter 'url' is null or empty.");
    }

    String username = getParam("username");
    if (util.isNullOrEmpty(username)) {
      throw new IllegalArgumentException("Required parameter 'username' is null or empty.");
    }
    
    UserAuthToken userAuthToken = authTokenDao.getUserAuthToken(username);    
    
    if (!userAuthToken.getAuthSubToken().isEmpty()) {
      apiManager.setAuthSubToken(userAuthToken.getAuthSubToken());
    } else {
      apiManager.setClientLoginToken(userAuthToken.getClientLoginToken());
    }    
    
    String captionTrack = apiManager.getCaptionTrack(url);

    if (captionTrack != null) {
      json.put("captionTrack", captionTrack);
    } else {
      throw new IllegalArgumentException(String.format("Unable to retrieve caption track from %s",
          url));
    }

    return json;
  }
}
