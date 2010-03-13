package com.google.ytd.command;

import com.google.gdata.data.media.mediarss.MediaThumbnail;
import com.google.gdata.data.youtube.VideoEntry;
import com.google.gdata.data.youtube.VideoFeed;
import com.google.inject.Inject;
import com.google.ytd.util.Util;
import com.google.ytd.youtube.YouTubeApiHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetYouTubeVideos extends Command {
  private YouTubeApiHelper apiManager = null;
  
  @Inject
  private Util util;
  
  @Inject
  public GetYouTubeVideos(YouTubeApiHelper apiManager) {
    this.apiManager = apiManager;
  }
  
  @Override
  public JSONObject execute() throws JSONException {
    JSONObject json = new JSONObject();
    
    String username = getParam("username");
    if (util.isNullOrEmpty(username)) {
      throw new IllegalArgumentException("Required parameter 'username' is null or empty.");
    }
    
    VideoFeed uploadsFeed = apiManager.getUploadsFeed(username);
    if (uploadsFeed != null) {
    	HashMap<String, Map<String, String>> videoIdToMetadata =
    		new HashMap<String, Map<String, String>>();
      for (VideoEntry videoEntry : uploadsFeed.getEntries()) {
        // Videos with a yt:state tag are not playable, so skip them.
        if (videoEntry.getPublicationState() == null) {
          HashMap<String, String> metadata = new HashMap<String, String>();

          metadata.put("title", videoEntry.getMediaGroup().getTitle().getPlainTextContent());
          metadata.put("videoUrl", videoEntry.getHtmlLink().getHref());
          List<MediaThumbnail> thumbnails = videoEntry.getMediaGroup().getThumbnails();
          if (thumbnails.size() > 0) {
          	metadata.put("thumbnailUrl",
          			videoEntry.getMediaGroup().getThumbnails().get(0).getUrl());
          }
          
          videoIdToMetadata.put(videoEntry.getMediaGroup().getVideoId(), metadata);
        }
      }
      
      json.put("videos", videoIdToMetadata);
    } else {
      json.put("error", "Unable to retrieve YouTube videos.");
    }
    
    return json;
  }
}
