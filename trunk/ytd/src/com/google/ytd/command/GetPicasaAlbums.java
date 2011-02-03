package com.google.ytd.command;

import com.google.gdata.data.photos.AlbumEntry;
import com.google.inject.Inject;
import com.google.ytd.picasa.PicasaApiHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetPicasaAlbums extends Command {
  private PicasaApiHelper apiManager = null;
  private String REJECTED_ALBUM_FORMAT = "%s (Rejected)";
  private String UNREVIEWED_ALBUM_FORMAT = "%s (Unreviewed)";
  
  @Inject
  public GetPicasaAlbums(PicasaApiHelper apiManager) {
    this.apiManager = apiManager;
  }

  @Override
  public JSONObject execute() throws JSONException {
    JSONObject json = new JSONObject();

    List<AlbumEntry> albumEntries = apiManager.getAllAlbums();
    if (albumEntries != null) {
      if (albumEntries.size() > 0) {
        // We want to determine the related (Rejected) and (Unreviewed) albums for each main album.
        // A HashMap and title lookups is a bit hacky, but it's the only real solution.
        HashMap<String, String> albumNameToId = new HashMap<String, String>();
        for (AlbumEntry albumEntry : albumEntries) {
          albumNameToId.put(albumEntry.getTitle().getPlainText(), albumEntry.getGphotoId());
        }
        
        HashMap<String, Map<String, String>> albumIdToMetadata =
          new HashMap<String, Map<String, String>>();
        for (AlbumEntry albumEntry : albumEntries) {
          if (albumEntry.getAccess().equals("public")) {
            HashMap<String, String> metadata = new HashMap<String, String>();
            String publicAlbumTitle = albumEntry.getTitle().getPlainText();
            
            String rejectedAlbumId = albumNameToId.get(String.format(REJECTED_ALBUM_FORMAT,
                publicAlbumTitle));
            String unreviewedAlbumId = albumNameToId.get(String.format(UNREVIEWED_ALBUM_FORMAT,
                publicAlbumTitle));
            
            if (rejectedAlbumId != null && unreviewedAlbumId != null) {
              metadata.put("title", publicAlbumTitle);
              metadata.put("rejectedAlbumId", rejectedAlbumId);
              metadata.put("unreviewedAlbumId", unreviewedAlbumId);
              
              albumIdToMetadata.put(albumEntry.getGphotoId(), metadata);
            }
          }
        }

        json.put("albums", albumIdToMetadata);
      } else {
        json.put("error", "There are no Picasa albums in your account.");
      }
    } else {
      json.put("error", "Unable to retrieve Picasa albums.");
    }

    return json;
  }
}