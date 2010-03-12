package com.google.ytd.command;

import com.google.gdata.util.ServiceException;
import com.google.inject.Inject;
import com.google.ytd.dao.UserAuthTokenDao;
import com.google.ytd.util.Util;
import com.google.ytd.youtube.YouTubeApiHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UpdateYouTubeCaptionTrack extends Command {
  private static final Logger LOG = Logger.getLogger(UpdateYouTubeCaptionTrack.class.getName());
  
  private static final String CAPTION_CONTENT_TYPE = "application/vnd.youtube.timedtext; " +
  		"charset=UTF-8";
  
  private YouTubeApiHelper apiManager = null;
  
  @Inject
  private Util util;
  
  @Inject
  public UpdateYouTubeCaptionTrack(UserAuthTokenDao authTokenDao, YouTubeApiHelper apiManager) {
    this.apiManager = apiManager;
  }
  
  @Override
  public JSONObject execute() throws JSONException {
    JSONObject json = new JSONObject();
    
    String videoId = getParam("videoId");
    if (util.isNullOrEmpty(videoId)) {
      throw new IllegalArgumentException("Required parameter 'videoId' is null or empty.");
    }
    
    String authSubToken = getParam("authSubToken");
    if (util.isNullOrEmpty(authSubToken)) {
      throw new IllegalArgumentException("Required parameter 'authSubToken' is null or empty.");
    }
    
    String captionTrack = getParam("captionTrack");
    if (util.isNullOrEmpty(captionTrack)) {
      throw new IllegalArgumentException("Required parameter 'captionTrack' is null or empty.");
    }
    
    String languageCode = getParam("languageCode");
    if (util.isNullOrEmpty(languageCode)) {
      throw new IllegalArgumentException("Required parameter 'languageCode' is null or empty.");
    }
    
    apiManager.setToken(authSubToken);
    apiManager.setHeader("Content-Type", CAPTION_CONTENT_TYPE);
    apiManager.setHeader("Content-Language", languageCode);

    try {
      boolean success = apiManager.updateCaptionTrack(videoId, captionTrack);
      json.put("success", success);
    } catch (MalformedURLException e) {
      json.put("error", "YouTube API error: " + e.getMessage());
      LOG.log(Level.WARNING, "", e);
    } catch (IOException e) {
      json.put("error", "YouTube API error: " + e.getMessage());
      LOG.log(Level.WARNING, "", e);
    } catch (ServiceException e) {
      json.put("error", "YouTube API error: " + e.getMessage());
      LOG.log(Level.WARNING, "", e);
    }
    
    return json;
  }
}
