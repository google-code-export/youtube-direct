package com.google.yaw;

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
import com.google.gdata.client.youtube.YouTubeService;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.youtube.FormUploadToken;
import com.google.gdata.data.youtube.PlaylistEntry;
import com.google.gdata.data.youtube.PlaylistFeed;
import com.google.gdata.data.youtube.PlaylistLinkEntry;
import com.google.gdata.data.youtube.UserProfileEntry;
import com.google.gdata.data.youtube.VideoEntry;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;
import com.google.yaw.model.AdminConfig;

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
  //TODO: These should be CAPS.
  private static final String categoriesCacheKey = "categories";
  private static final String entryUrlFormat = "http://gdata.youtube.com/feeds/api/videos/%s";
  private static final String uploadsEntryUrlFormat = "http://gdata.youtube.com/feeds/api/" +
  		"users/%s/uploads/%s";
  private static final String playlistEntryUrlFormat = "http://gdata.youtube.com/feeds/api/" +
  		"playlists/%s";
  private static final String playlistFeed = "http://gdata.youtube.com/feeds/api/users/default/" +
  		"playlists";
  private static final String userEntry = "http://gdata.youtube.com/feeds/api/users/default";
  private static final String uploadToken = "http://gdata.youtube.com/action/GetUploadToken";
  private static final Logger log = Logger.getLogger(YouTubeApiManager.class.getName());

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
    }

    service = new YouTubeService(clientId, developerKey);
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
   * Set the username and password ClientLogin credentials.
   * @param username A valid YouTube username.
   * @param password The password associated with that username.
   * @throws AuthenticationException 
   */
  public void setLoginInfo(String username, String password) throws AuthenticationException {
    service.setUserCredentials(username, password);
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
      UserProfileEntry profile = service.getEntry(new URL(userEntry), UserProfileEntry.class);
      return profile.getUsername();
    } catch (MalformedURLException e) {
      log.warning(e.toString());
    }

    return null;
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
      URL uploadUrl = new URL(uploadToken);
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
    return String.format(entryUrlFormat, videoId);
  }
  
  public String generateUploadsVideoEntryUrl(String videoId) {
    return generateUploadsVideoEntryUrl("default", videoId);
  }
  
  public String generateUploadsVideoEntryUrl(String username, String videoId) {
    return String.format(uploadsEntryUrlFormat, username, videoId);
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
  
  public boolean insertVideoIntoPlaylist(String playlistId, String videoId) {
    VideoEntry videoEntry = getVideoEntry(videoId);
    
    if (videoEntry != null) {
      PlaylistEntry playlistEntry = new PlaylistEntry(videoEntry);
      
      //TODO: Check to make sure video isn't already in playlist?
      
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
  
  public String getPlaylistFeedUrl(String playlistId) {
    return String.format(playlistEntryUrlFormat, playlistId);
  }
  
  public String createPlaylist(String title, String description) {
    PlaylistLinkEntry newEntry = new PlaylistLinkEntry();
    newEntry.setTitle(new PlainTextConstruct(title));
    newEntry.setSummary(new PlainTextConstruct(description));

    try {
      PlaylistLinkEntry createdEntry = service.insert(new URL(playlistFeed), newEntry);
      String id = createdEntry.getPlaylistId();

      log.info(String.format("Created new playlist with id '%s'.", id));      
      return id;
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
      List<String> cachedCategories = (List<String>) cache.get(categoriesCacheKey);

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
        cache.put(categoriesCacheKey, categories);
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
