package com.google.ytd.youtube;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gdata.data.youtube.VideoEntry;
import com.google.gdata.data.youtube.YouTubeMediaContent;
import com.google.gdata.util.common.util.Base64;
import com.google.gdata.util.common.util.Base64DecoderException;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.ytd.dao.AdminConfigDao;
import com.google.ytd.model.AdminConfig;
import com.google.ytd.util.Util;

/**
 * Servlet that retrieves the video download link for a video and redirects the browser there.
 * 
 * Video download links are only available for organizations who have partnered with YouTube and
 * have their own "strong authentication" key.
 */
@Singleton
public class VideoDownloadRedirect extends HttpServlet {
  private static final Logger log = Logger.getLogger(VideoDownloadRedirect.class.getName());
  private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
  
  private Util util = null;
  private AdminConfigDao adminConfigDao = null;
  
  @Inject
  public VideoDownloadRedirect(Util util, AdminConfigDao adminConfigDao) {
    this.util = util;
    this.adminConfigDao = adminConfigDao;
  }  
  
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    try {
      String id = req.getParameter("id");
      if (util.isNullOrEmpty(id)) {
        throw new IllegalArgumentException("'id' parameter is null or empty.");
      }
      
      AdminConfig adminConfig = adminConfigDao.getAdminConfig();
      
      YouTubeApiHelper apiManager = new YouTubeApiHelper(adminConfig.getClientId());
      apiManager.setHeader("X-GData-Device",
              generateStrongAuthHeader(apiManager.generateVideoEntryUrl(id)));
      
      VideoEntry videoEntry = apiManager.getVideoEntry(id);
      if (videoEntry == null) {
        throw new IllegalArgumentException(String.format("Couldn't get video with id '%s'. " +
            "If it was recently uploaded to YouTube, try again in a few minutes.", id));
      }
      
      List<YouTubeMediaContent> mediaContents = videoEntry.getMediaGroup().getYouTubeContents();
      boolean found = false;
      for (YouTubeMediaContent mediaContent : mediaContents) {
        if (mediaContent.getType().equals("video/mp4")) {
          resp.sendRedirect(mediaContent.getUrl());
          found = true;
        }
      }
      
      if (!found) {
        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Could not find video/mp4 " +
            "content link in YouTube API response.");
      }
    } catch (IllegalArgumentException e) {
      log.log(Level.WARNING, "", e);
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
    } catch (MalformedURLException e) {
      log.log(Level.WARNING, "", e);
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
    } catch (NoSuchAlgorithmException e) {
      log.log(Level.WARNING, "", e);
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
    } catch (InvalidKeyException e) {
      log.log(Level.WARNING, "", e);
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
    } catch (Base64DecoderException e) {
      log.log(Level.WARNING, "", e);
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }
  
  private String generateStrongAuthHeader(String requestUrl)
      throws MalformedURLException, NoSuchAlgorithmException, InvalidKeyException,
      Base64DecoderException {
    String deviceId = "AOuj_RodAp9R5_GQJcmRCgJPhy1IVvZA8uZ7SQUc9VxpSy03HZ6iSoUfgTinJ69sYeMW-ICy6kpqxVfe94rH_mYDjz2v0F2kq721C_lYCqsiw9kMKFn46ts";
    String deviceKey = "5J3DNFw3obP1PjQzgtxCbFnVsq0=";
    
    byte[] decodedDeviceKey = Base64.decode(deviceKey);

    URL url = new URL(requestUrl);
    String path = url.getPath();
    
    SecretKeySpec signingKey = new SecretKeySpec(decodedDeviceKey, HMAC_SHA1_ALGORITHM);
    Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
    mac.init(signingKey);
    byte[] hmacBytes = mac.doFinal(path.getBytes());
    String signature = Base64.encode(hmacBytes);
    
    return String.format("device-id=\"%s\", data=\"%s\"", deviceId, signature);
  }
}