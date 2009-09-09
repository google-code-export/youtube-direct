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
import com.google.gdata.data.youtube.FormUploadToken;
import com.google.gdata.data.youtube.UserProfileEntry;
import com.google.gdata.data.youtube.VideoEntry;
import com.google.gdata.util.ServiceException;

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
  private static final String categoriesCacheKey = "categories";
  private static final String entryUrlFormat = "http://gdata.youtube.com/feeds/api/users/default/uploads/%s";
  private static final String userEntry = "http://gdata.youtube.com/feeds/api/users/default";
  private static final String uploadToken = "http://gdata.youtube.com/action/GetUploadToken";
  private static final Logger log = Logger.getLogger(YouTubeApiManager.class.getName());

  /**
   * Create a new instance of the class, initializing a YouTubeService object
   * with parameters specified in appengine-web.xml
   */
  public YouTubeApiManager() {
    String clientId = System.getProperty("com.google.yaw.YTClientID");
    String developerKey = System.getProperty("com.google.yaw.YTDeveloperKey");

    if (Util.isNullOrEmpty(clientId)) {
      log.warning("com.google.yaw.YTClientID property is not set.");
    }

    if (Util.isNullOrEmpty(developerKey)) {
      log.warning("com.google.yaw.YTDeveloperKey property is not set.");
    }

    service = new YouTubeService(clientId, developerKey);
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

  /**
   * Gets a YouTube video entry given a specific video id. Constructs the entry
   * URL based on a hardcoded URL prefix, which might need to be changed in the
   * future.
   * 
   * @param videoId
   *          The YouTube video id of a given video.
   * @return A VideoEntry representing the video in question, or null.
   */
  public VideoEntry getVideoEntry(String videoId) {
    String entryUrl = String.format(entryUrlFormat, videoId);

    try {
      return service.getEntry(new URL(entryUrl), VideoEntry.class);
    } catch (MalformedURLException e) {
      log.log(Level.WARNING, "", e);
    } catch (IOException e) {
      log.log(Level.WARNING, "", e);
    } catch (ServiceException e) {
      // This may be thrown if the video is not found, i.e. because it is not
      // done processing.
      // We don't need to log it at WARNING level.
      log.log(Level.FINE, "", e);
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
