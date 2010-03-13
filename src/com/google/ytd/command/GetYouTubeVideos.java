package com.google.ytd.command;

import com.google.gdata.data.media.mediarss.MediaThumbnail;
import com.google.gdata.data.youtube.VideoEntry;
import com.google.gdata.data.youtube.VideoFeed;
import com.google.gdata.data.youtube.YouTubeMediaGroup;
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
        if (!videoEntry.isDraft()) {
          HashMap<String, String> metadata = new HashMap<String, String>();
          YouTubeMediaGroup mediaGroup = videoEntry.getMediaGroup();
          
          metadata.put("title", mediaGroup.getTitle().getPlainTextContent());
          metadata.put("videoUrl", videoEntry.getHtmlLink().getHref());
          metadata.put("description", mediaGroup.getDescription().getPlainTextContent());
          metadata.put("duration", mediaGroup.getDuration().toString());
          metadata.put("published", videoEntry.getPublished().toUiString());
          
          List<MediaThumbnail> thumbnails = mediaGroup.getThumbnails();
          if (thumbnails.size() > 0) {
          	metadata.put("thumbnailUrl",
          			mediaGroup.getThumbnails().get(0).getUrl());
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
