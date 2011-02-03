package com.google.ytd.command;

import com.google.gdata.data.youtube.PlaylistLinkEntry;
import com.google.gdata.data.youtube.PlaylistLinkFeed;
import com.google.inject.Inject;
import com.google.ytd.youtube.YouTubeApiHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class GetYouTubePlaylists extends Command {
  private YouTubeApiHelper apiManager = null;

  @Inject
  public GetYouTubePlaylists(YouTubeApiHelper apiManager) {
    this.apiManager = apiManager;
  }

  @Override
  public JSONObject execute() throws JSONException {
    JSONObject json = new JSONObject();

    PlaylistLinkFeed playlistFeed = apiManager.getDefaultUsersPlaylistFeed();
    if (playlistFeed != null) {
      if (playlistFeed.getEntries().size() > 0) {
        HashMap<String, Map<String, String>> playlistIdToMetadata = new HashMap<String, Map<String, String>>();
        for (PlaylistLinkEntry playlistEntry : playlistFeed.getEntries()) {
          HashMap<String, String> metadata = new HashMap<String, String>();
          
          metadata.put("title", playlistEntry.getTitle().getPlainText());
          metadata.put("description", playlistEntry.getSummary().getPlainText());

          playlistIdToMetadata.put(playlistEntry.getPlaylistId(), metadata);
        }

        json.put("playlists", playlistIdToMetadata);
      } else {
        json.put("error", "There are no playlists in your account.");
      }
    } else {
      json.put("error", "Unable to retrieve YouTube playlists.");
    }

    return json;
  }
}
