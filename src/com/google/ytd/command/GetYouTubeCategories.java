package com.google.ytd.command;

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

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.appengine.api.memcache.stdimpl.GCacheFactory;

public class GetYouTubeCategories extends Command {
  private static final Logger LOG = Logger.getLogger(GetYouTubeCategories.class.getName());

  private static final String CATEGORIES_CACHE_KEY = "categories";
  private static final String CATEGORIES_URL =
    "http://gdata.youtube.com/schemas/2007/categories.cat";
  private static final int CACHE_EXPIRATION_SECONDS = 60 * 60 * 24; // One day.

  @Override
  public JSONObject execute() throws JSONException {
    JSONObject json = new JSONObject();
    
    List<String> categories = getCategoryCodes();
    json.put("categories", categories);
    
    return json;
  }
  
  @SuppressWarnings("unchecked")
  public List<String> getCategoryCodes() {
    List<String> categories;
    Cache cache = null;

    try {
      Map cachedProperties = new HashMap();
      cachedProperties.put(GCacheFactory.EXPIRATION_DELTA, CACHE_EXPIRATION_SECONDS);
      cache = CacheManager.getInstance().getCacheFactory().createCache(cachedProperties);
      List<String> cachedCategories = (List<String>) cache.get(CATEGORIES_CACHE_KEY);

      if (cachedCategories != null) {
        return cachedCategories;
      }
    } catch (CacheException e) {
      LOG.log(Level.WARNING, "", e);
    }

    categories = new ArrayList<String>();

    try {
      URL url = new URL(CATEGORIES_URL);
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
      LOG.log(Level.WARNING, "", e);
    } catch (ParserConfigurationException e) {
      LOG.log(Level.WARNING, "", e);
    } catch (IOException e) {
      LOG.log(Level.WARNING, "", e);
    } catch (SAXException e) {
      LOG.log(Level.WARNING, "", e);
    }

    return categories;
  }
}
