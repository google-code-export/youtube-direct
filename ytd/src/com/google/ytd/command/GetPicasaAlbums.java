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
        HashMap<String, String> albumNameToUrl = new HashMap<String, String>();
        for (AlbumEntry albumEntry : albumEntries) {
          albumNameToUrl.put(albumEntry.getTitle().getPlainText(),
              albumEntry.getFeedLink().getHref());
        }
        
        HashMap<String, Map<String, String>> albumUrlToMetadata =
          new HashMap<String, Map<String, String>>();
        for (AlbumEntry albumEntry : albumEntries) {
          if (albumEntry.getAccess().equals("public")) {
            HashMap<String, String> metadata = new HashMap<String, String>();
            String publicAlbumTitle = albumEntry.getTitle().getPlainText();
            
            String rejectedAlbumUrl = albumNameToUrl.get(String.format(REJECTED_ALBUM_FORMAT,
                publicAlbumTitle));
            String unreviewedAlbumUrl = albumNameToUrl.get(String.format(UNREVIEWED_ALBUM_FORMAT,
                publicAlbumTitle));
            
            if (rejectedAlbumUrl != null && unreviewedAlbumUrl != null) {
              metadata.put("title", publicAlbumTitle);
              metadata.put("rejectedAlbumUrl", rejectedAlbumUrl);
              metadata.put("unreviewedAlbumUrl", unreviewedAlbumUrl);
              
              albumUrlToMetadata.put(albumEntry.getFeedLink().getHref(), metadata);
            }
          }
        }

        json.put("albums", albumUrlToMetadata);
      } else {
        json.put("error", "There are no Picasa albums in your account.");
      }
    } else {
      json.put("error", "Unable to retrieve Picasa albums.");
    }

    return json;
  }
}