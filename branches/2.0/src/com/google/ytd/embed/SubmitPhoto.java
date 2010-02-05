/* Copyright (c) 2010 Google Inc.
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

package com.google.ytd.embed;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.ytd.dao.AdminConfigDao;
import com.google.ytd.dao.UserAuthTokenDao;
import com.google.ytd.guice.ProductionModule;
import com.google.ytd.model.PhotoSubmission;
import com.google.ytd.model.UserSession;
import com.google.ytd.util.EmailUtil;
import com.google.ytd.util.PmfUtil;
import com.google.ytd.util.Util;
import com.google.ytd.youtube.YouTubeApiHelper;

/**
 * Servlet that handles the submission of photos. It creates a new PhotoSubmission object 
 * and saves it to the datastore. The response needs to be a 30x redirect, as per the
 * BlobStore API.
 */
@Singleton
public class SubmitPhoto extends HttpServlet {
  private static final Logger log = Logger.getLogger(SubmitPhoto.class.getName());

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    try {
      Util util = Util.get();
      
      // Create a new PMF because unfortunately we don't have access to the Guice version.
      PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory(
          "transactions-optional");
      PmfUtil pmfUtil = new PmfUtil(pmf);
      
      // TODO: Pass in assignmentId in emebed.jsp
      String assignmentId = "13";//req.getParameter("assignmentId");
      if (util.isNullOrEmpty(assignmentId)) {
        throw new IllegalArgumentException("'assignmentId' is null or empty.");
      }
      
      String title = req.getParameter("title");
      if (util.isNullOrEmpty(title)) {
        throw new IllegalArgumentException("'title' is null or empty.");
      }
      
      String description = req.getParameter("description");
      if (util.isNullOrEmpty(description)) {
        throw new IllegalArgumentException("'description' is null or empty.");
      }
      
      String location = req.getParameter("location");
      
      String email = req.getParameter("uploadEmail");
      if (util.isNullOrEmpty(email)) {
        throw new IllegalArgumentException("'uploadEmail' is null or empty.");
      }
      
      String batchId = null;
      
      BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
      
      Map<String, BlobKey> blobs = blobstoreService.getUploadedBlobs(req);
      for (Entry<String, BlobKey> entry : blobs.entrySet()) {
        log.info(String.format("Processing file form element '%s'.", entry.getKey()));
        
        BlobKey blobKey = entry.getValue();
        
        // Use the String representation of the first image's BlobKey as a unique id for the
        // batch of multiple uploads.
        if (batchId == null) {
          batchId = blobKey.toString();
        }
        
        PhotoSubmission photoSubmission = new PhotoSubmission(Long.parseLong(assignmentId),
            blobKey, batchId, email, title, description, location);
        
        pmfUtil.persistJdo(photoSubmission);
      }
    } catch (IllegalArgumentException e) {
      log.log(Level.WARNING, "", e);
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
    } finally {
      
    }
    
    
    /*try {
      JSONObject jsonObj = new JSONObject(json);

      String videoId = jsonObj.getString("videoId");
      String location = jsonObj.getString("location");
      String date = jsonObj.getString("date");
      String email = jsonObj.getString("email");

      // Only check for required parameters 'videoId'.
      if (util.isNullOrEmpty(videoId)) {
        throw new IllegalArgumentException("'videoId' parameter is null or empty.");
      }

      // Grab user session meta data
      UserSession userSession = userSessionManager.getUserSession(req);
      String youTubeName = userSession.getMetaData("youTubeName");
      String authSubToken = userSession.getMetaData("authSubToken");
      String assignmentId = userSession.getMetaData("assignmentId");
      String articleUrl = userSession.getMetaData("articleUrl");

      apiManager.setToken(authSubToken);

      VideoEntry videoEntry = apiManager.getUploadsVideoEntry(videoId);

      if (videoEntry == null) {
        JSONObject responseJsonObj = new JSONObject();
        responseJsonObj.put("success", "false");
        responseJsonObj.put("message", "Cannot find this video in your account.");

        resp.setContentType("text/javascript");
        resp.getWriter().println(responseJsonObj.toString());
      } else {
        String title = videoEntry.getTitle().getPlainText();
        String description = videoEntry.getMediaGroup().getDescription().getPlainTextContent();

        List<String> tags = videoEntry.getMediaGroup().getKeywords().getKeywords();
        String sortedTags = util.sortedJoin(tags, ",");

        long viewCount = -1;

        YtStatistics stats = videoEntry.getStatistics();
        if (stats != null) {
          viewCount = stats.getViewCount();
        }

        VideoSubmission submission = new VideoSubmission(Long.parseLong(assignmentId));

        submission.setArticleUrl(articleUrl);
        submission.setVideoId(videoId);
        submission.setVideoTitle(title);
        submission.setVideoDescription(description);
        submission.setVideoTags(sortedTags);
        submission.setVideoLocation(location);
        submission.setVideoDate(date);
        submission.setYouTubeName(youTubeName);

        userAuthTokenDao.setUserAuthToken(youTubeName, authSubToken);

        submission.setViewCount(viewCount);
        submission.setVideoSource(VideoSubmission.VideoSource.EXISTING_VIDEO);
        submission.setNotifyEmail(email);

        AdminConfig adminConfig = adminConfigDao.getAdminConfig();
        if (adminConfig.getModerationMode() == AdminConfig.ModerationModeType.NO_MOD.ordinal()) {
          // NO_MOD is set, auto approve all submission
          // TODO: This isn't enough, as the normal approval flow (adding the
          // branding, tags, emails,
          // etc.) isn't taking place.
          submission.setStatus(VideoSubmission.ModerationStatus.APPROVED);
        }

        pmfUtil.persistJdo(submission);

        emailUtil.sendNewSubmissionEmail(submission);

        JSONObject responseJsonObj = new JSONObject();
        responseJsonObj.put("success", "true");

        resp.setContentType("text/javascript");
        resp.getWriter().println(responseJsonObj.toString());
      }
    } catch (IllegalArgumentException e) {
      log.log(Level.FINE, "", e);
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
    } catch (JSONException e) {
      log.log(Level.WARNING, "", e);
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
    }*/
  }
}