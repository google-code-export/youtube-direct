/* Copyright (c) 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.ytd;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.google.appengine.api.memcache.stdimpl.GCacheFactory;
import com.google.gdata.client.Service.GDataRequest;
import com.google.gdata.client.youtube.YouTubeService;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.youtube.FormUploadToken;
import com.google.gdata.data.youtube.PlaylistEntry;
import com.google.gdata.data.youtube.PlaylistFeed;
import com.google.gdata.data.youtube.PlaylistLinkEntry;
import com.google.gdata.data.youtube.UserProfileEntry;
import com.google.gdata.data.youtube.VideoEntry;
import com.google.gdata.util.ServiceException;
import com.google.ytd.model.AdminConfig;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Class to handle interfacing with the Google Data Java Client Library's
 * YouTube support.
 */
public class YouTubeApiManager {
  private YouTubeService service = null;
  private static final Logger log = Logger.getLogger(YouTubeApiManager.class.getName());
  
  // CONSTANTS
  private static final String CATEGORIES_CACHE_KEY = "categories";
  private static final String ENTRY_URL_FORMAT = "http://gdata.youtube.com/feeds/api/videos/%s";
  private static final String UPLOADS_URL_FORMAT = "http://gdata.youtube.com/feeds/api/" +
  		"users/%s/uploads/%s";
  private static final String PLAYLIST_ENTRY_URL_FORMAT = "http://gdata.youtube.com/feeds/api/" +
  		"playlists/%s";
  private static final String PLAYLIST_FEED_URL = "http://gdata.youtube.com/feeds/api/users/" +
  		"default/playlists";
  private static final String USER_ENTRY_URL = "http://gdata.youtube.com/feeds/api/users/default";
  private static final String UPLOAD_TOKEN_URL = "http://gdata.youtube.com/action/GetUploadToken";
  private static final String MODERATION_FEED_ENTRY_URL_FORMAT = "http://gdata.youtube.com/feeds/" +
  		"api/products/default/videos/%s";
  private static final String UPDATED_ENTRY_ATOM_FORMAT = "<entry xmlns='http://www.w3.org/2005/" +
  		"Atom' xmlns:yt='http://gdata.youtube.com/schemas/2007'><yt:moderationStatus>%s" +
  		"</yt:moderationStatus></entry>";
  private static final String MODERATION_ACCEPTED = "accepted";
  private static final String MODERATION_REJECTED = "rejected";
  

  /**
   * Create a new instance of the class, initializing a YouTubeService object
   * with parameters specified in appengine-web.xml
   */
  public YouTubeApiManager() {
    AdminConfig admingConfig = Util.getAdminConfig();
    
    String clientId = admingConfig.getClientId();
    String developerKey = admingConfig.getDeveloperKey();        
    
    if (Util.isNullOrEmpty(clientId)) {    
      log.warning("clientId settings property is null or empty.");
    }

    if (Util.isNullOrEmpty(developerKey)) {
      log.warning("developerKey settings property is null or empty.");
      service = new YouTubeService(clientId);
    } else {
      service = new YouTubeService(clientId, developerKey);
    }
  }
  
  public YouTubeApiManager(String clientId) {
    if (Util.isNullOrEmpty(clientId)) {
      log.warning("clientId parameter is null or empty.");
    }

    service = new YouTubeService(clientId);
  }

  /**
   * Sets the AuthSub token to use for API requests.
   * 
   * @param token
   *          The token to use.
   */
  public void setToken(String token) {
    service.setAuthSubToken(token);
  }
  
  /**
   * Sets an arbitrary header for all outgoing requests using this service instance.
   * 
   * @param header The name of the header.
   * @param value The header's value.
   */
  public void setHeader(String header, String value) {
    service.getRequestFactory().setHeader(header, value);
  }

  /**
   * Gets the username for the authenticated user, assumes that setToken() has
   * already been called to provide authentication.
   * 
   * @return The current username for the authenticated user.
   * @throws ServiceException
   * @throws IOException
   */
  public String getCurrentUsername() throws IOException, ServiceException {
    try {
      UserProfileEntry profile = service.getEntry(new URL(USER_ENTRY_URL), UserProfileEntry.class);
      return profile.getUsername();
    } catch (MalformedURLException e) {
      log.log(Level.WARNING, "", e);
    }

    return null;
  }
  
  /**
   * Sets the value for video moderation, which controls whether partner branding shows up on the
   * video's YouTube.com watch page.
   * 
   * This request needs to be made with the authorization of the account that owns the developr
   * token used to originally upload the video. Also, the video must have at least one developer
   * tag set at the time it was uploaded.
   * 
   * @param videoId The YouTube id of the video to moderate.
   * @param isApproved true if this video is approved, and false if not.
   */
  public void updateModeration(String videoId, boolean isApproved) {
    log.info(String.format("Setting moderation of video id '%s' to '%s'.", videoId, isApproved));
    
    String entryUrl = String.format(MODERATION_FEED_ENTRY_URL_FORMAT, videoId);
    String updatedEntry;
    if (isApproved) {
      updatedEntry = String.format(UPDATED_ENTRY_ATOM_FORMAT, MODERATION_ACCEPTED);
    } else {
      updatedEntry = String.format(UPDATED_ENTRY_ATOM_FORMAT, MODERATION_REJECTED);
    }
    
    try {
      GDataRequest request = service.createUpdateRequest(new URL(entryUrl));
      request.getRequestStream().write(updatedEntry.getBytes());
      request.execute();
    } catch (MalformedURLException e) {
      log.log(Level.WARNING, "", e);
    } catch (IOException e) {
      log.log(Level.WARNING, "", e);
    } catch (ServiceException e) {
      log.log(Level.WARNING, "", e);
    }
  }

  /**
   * Submits video metadata to YouTube to get an upload token and URL.
   * 
   * @param newEntry
   *          The VideoEntry containing all video metadata for the upload
   * @return A FormUploadToken used when uploading a video to YouTube.
   */
  public FormUploadToken getFormUploadToken(VideoEntry newEntry) {
    try {
      URL uploadUrl = new URL(UPLOAD_TOKEN_URL);
      return  service.getFormUploadToken(uploadUrl, newEntry);
    } catch (MalformedURLException e) {
      log.log(Level.WARNING, "", e);
    } catch (ServiceException e) {
      log.log(Level.WARNING, "", e);
    } catch (IOException e) {
      log.log(Level.WARNING, "", e);
    }

    return null;
  }
  
  public String generateVideoEntryUrl(String videoId) {
    return String.format(ENTRY_URL_FORMAT, videoId);
  }
  
  public String generateUploadsVideoEntryUrl(String videoId) {
    return generateUploadsVideoEntryUrl("default", videoId);
  }
  
  public String generateUploadsVideoEntryUrl(String username, String videoId) {
    return String.format(UPLOADS_URL_FORMAT, username, videoId);
  }
  
  public VideoEntry getUploadsVideoEntry(String videoId) {
    String entryUrl = generateUploadsVideoEntryUrl(videoId);
    
    return makeVideoEntryRequest(entryUrl);
  }
  
  public VideoEntry getUploadsVideoEntry(String username, String videoId) {
    String entryUrl = generateUploadsVideoEntryUrl(username, videoId);
    
    return makeVideoEntryRequest(entryUrl);
  }
  
  public VideoEntry getVideoEntry(String videoId) {
    String entryUrl = generateVideoEntryUrl(videoId);
    
    return makeVideoEntryRequest(entryUrl);
  }

  /**
   * Gets a YouTube video entry given a specific video id. Constructs the entry
   * URL based on a hardcoded URL prefix, which might need to be changed in the
   * future.
   * 
   * @param entryUrl A URL string representing a GData video entity.
   * @return A VideoEntry representing the video in question, or null.
   */
  public VideoEntry makeVideoEntryRequest(String entryUrl) {
    try {
      return service.getEntry(new URL(entryUrl), VideoEntry.class);
    } catch (MalformedURLException e) {
      log.log(Level.WARNING, "", e);
    } catch (IOException e) {
      log.log(Level.WARNING, "", e);
    } catch (ServiceException e) {
      // This may be thrown if the video is not found, i.e. because it is not done processing.
      // We don't need to log it at WARNING level.
      //TODO: Propogate AuthenticationExceptions so the calling code can invalidate the token.
      log.log(Level.INFO, "", e);
    }

    log.info(String.format("Couldn't get video entry from %s.", entryUrl));
    return null;
  }
  
  public PlaylistEntry getVideoInPlaylist(String playlistId, String videoId) {
    String playlistUrl = getPlaylistFeedUrl(playlistId);

    try {
      PlaylistFeed playlistFeed = service.getFeed(new URL(playlistUrl), PlaylistFeed.class);

      // TODO: Is there a better way to find the video in the playlist than O(n) looping?
      for(PlaylistEntry playlistEntry : playlistFeed.getEntries()) {
        if (playlistEntry.getMediaGroup().getVideoId().equals(videoId)) {
          return playlistEntry;
        }
      }
    } catch (MalformedURLException e) {
      log.log(Level.WARNING, "", e);
    } catch (IOException e) {
      log.log(Level.WARNING, "", e);
    } catch (ServiceException e) {
      //TODO: Propogate AuthenticationExceptions so the calling code can invalidate the token.
      log.log(Level.WARNING, "", e);
    }

    return null;
  }
  
  public boolean insertVideoIntoPlaylist(String playlistId, String videoId) {
    VideoEntry videoEntry = getVideoEntry(videoId);
    
    if (videoEntry != null) {
      PlaylistEntry playlistEntry = new PlaylistEntry(videoEntry);
      
      if(getVideoInPlaylist(playlistId, videoId) != null) {
        log.warning(String.format("Video id '%s' is already in playlist id '%s'.", videoId,
                playlistId));
        // Return true here, so that the video is flagged as being in the playlist.
        return true;
      }
      
      try {
        service.insert(new URL(getPlaylistFeedUrl(playlistId)), playlistEntry);
        
        log.info(String.format("Inserted video id '%s' into playlist id '%s'.", videoId,
                playlistId));
        
        return true;
      } catch (MalformedURLException e) {
        log.log(Level.WARNING, "", e);
      } catch (IOException e) {
        log.log(Level.WARNING, "", e);
      } catch (ServiceException e) {
        // This may be thrown if the video is not found, i.e. because it is not done processing.
        // We don't need to log it at WARNING level.
        //TODO: Propogate AuthenticationExceptions so the calling code can invalidate the token.
        log.log(Level.WARNING, "", e);
      }
    }
    
    return false;
  }
  
  public boolean removeVideoFromPlaylist(String playlistId, String videoId) {
    try {
      PlaylistEntry playlistEntry = getVideoInPlaylist(playlistId, videoId);

      if (playlistEntry == null) {
        log.warning(String.format("Could not find video id '%s' in playlist id '%s'.", videoId,
                playlistId));
        return false;
      } else {
        playlistEntry.delete();

        log.info(String.format("Removed video id '%s' from playlist id '%s'.", videoId,
                playlistId));

        return true;
      }
    } catch (MalformedURLException e) {
      log.log(Level.WARNING, "", e);
    } catch (IOException e) {
      log.log(Level.WARNING, "", e);
    } catch (ServiceException e) {
      //TODO: Propogate AuthenticationExceptions so the calling code can invalidate the token.
      log.log(Level.WARNING, "", e);
    }

    return false;
  }
  
  public String getPlaylistFeedUrl(String playlistId) {
    return String.format(PLAYLIST_ENTRY_URL_FORMAT, playlistId);
  }
  
  public String createPlaylist(String title, String description) {
    PlaylistLinkEntry newEntry = new PlaylistLinkEntry();
    newEntry.setTitle(new PlainTextConstruct(title));
    newEntry.setSummary(new PlainTextConstruct(description));

    try {
      PlaylistLinkEntry createdEntry = service.insert(new URL(PLAYLIST_FEED_URL), newEntry);
      String id = createdEntry.getPlaylistId();

      log.info(String.format("Created new playlist with id '%s'.", id));      
      return id;
    } catch (MalformedURLException e) {
      log.log(Level.WARNING, "", e);
    } catch (IOException e) {
      log.log(Level.WARNING, "", e);
    } catch (ServiceException e) {
      //TODO: Propogate AuthenticationExceptions so the calling code can invalidate the token.
      log.log(Level.WARNING, "", e);
    }
    
    return null;
  }

  @SuppressWarnings("unchecked")
  public static List<String> getCategoryCodes() {
    List<String> categories;
    Cache cache = null;

    try {
      Map cachedProperties = new HashMap();
      cachedProperties.put(GCacheFactory.EXPIRATION_DELTA, 60 * 60 * 24);
      cache = CacheManager.getInstance().getCacheFactory().createCache(cachedProperties);
      List<String> cachedCategories = (List<String>) cache.get(CATEGORIES_CACHE_KEY);

      if (cachedCategories != null) {
        return cachedCategories;
      }
    } catch (CacheException e) {
      log.log(Level.WARNING, "", e);
    }

    categories = new ArrayList<String>();

    try {
      URL url = new URL("http://gdata.youtube.com/schemas/2007/categories.cat");
      DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
      Document xmlDocument = docBuilder.parse(url.openStream());

      NodeList nodes = xmlDocument.getElementsByTagName("atom:category");
      for (int i = 0; i < nodes.getLength(); i++) {
        Node node = nodes.item(i);

        boolean isAssignable = false;
        NodeList childNodes = node.getChildNodes();
        for (int j = 0; j < childNodes.getLength(); j++) {
          Node childNode = childNodes.item(j);
          if (childNode.getNodeName().equals("yt:assignable")) {
            isAssignable = true;
          }
        }

        if (isAssignable) {
          NamedNodeMap attributes = node.getAttributes();
          Node termNode = attributes.getNamedItem("term");

          if (termNode != null) {
            categories.add(termNode.getTextContent());
          }
        }
      }

      Collections.sort(categories);

      if (cache != null) {
        cache.put(CATEGORIES_CACHE_KEY, categories);
      }
    } catch (MalformedURLException e) {
      log.log(Level.WARNING, "", e);
    } catch (ParserConfigurationException e) {
      log.log(Level.WARNING, "", e);
    } catch (IOException e) {
      log.log(Level.WARNING, "", e);
    } catch (SAXException e) {
      log.log(Level.WARNING, "", e);
    }

    return categories;
  }
}
