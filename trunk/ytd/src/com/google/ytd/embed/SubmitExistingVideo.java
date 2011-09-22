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

package com.google.ytd.embed;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gdata.data.youtube.VideoEntry;
import com.google.gdata.data.youtube.YtStatistics;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.ytd.dao.AdminConfigDao;
import com.google.ytd.dao.AssignmentDao;
import com.google.ytd.dao.UserAuthTokenDao;
import com.google.ytd.model.AdminConfig;
import com.google.ytd.model.Assignment;
import com.google.ytd.model.UserSession;
import com.google.ytd.model.VideoSubmission;
import com.google.ytd.util.EmailUtil;
import com.google.ytd.util.PmfUtil;
import com.google.ytd.util.Util;
import com.google.ytd.youtube.YouTubeApiHelper;

/**
 * Servlet that handles the submission of an existing YouTube video. It creates
 * a new VideoSubmission object and persists it to the datastore. The response
 * is the JSON representation of the new object.
 */
@Singleton
public class SubmitExistingVideo extends HttpServlet {
  private static final Logger log = Logger.getLogger(SubmitExistingVideo.class.getName());

  @Inject
  private Util util;
  @Inject
  private EmailUtil emailUtil;
  @Inject
  private PmfUtil pmfUtil;
  @Inject
  private UserSessionManager userSessionManager;
  @Inject
  private YouTubeApiHelper apiManager;
  @Inject
  private UserAuthTokenDao userAuthTokenDao;
  @Inject
  private AdminConfigDao adminConfigDao;
  @Inject
  private AssignmentDao assignmentDao;


  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String json = util.getPostBody(req);

    try {
      JSONObject jsonObj = new JSONObject(json);

      JSONArray videoIds = jsonObj.getJSONArray("videoIds");
      String location = jsonObj.getString("location");
      String phoneNumber = jsonObj.getString("phoneNumber");
      String date = jsonObj.getString("date");
      String email = jsonObj.getString("email");

      String assignmentId = null;
      if (jsonObj.has("assignmentId")) {
        assignmentId = jsonObj.getString("assignmentId");
      }
      
      if (videoIds.length() < 1) {
        throw new IllegalArgumentException("No video ids were provided.");
      }

      // Grab user session meta data
      UserSession userSession = userSessionManager.getUserSession(req);
      String youTubeName = userSession.getMetaData("youTubeName");
      String authSubToken = userSession.getMetaData("authSubToken");
      String articleUrl = userSession.getMetaData("articleUrl");

      // Assignment id might be set in the JSON object if there wasn't an assignment associated
      // with the embedded iframe, and the assignment was chosen at run time.
      if (util.isNullOrEmpty(assignmentId)) {
        assignmentId = userSession.getMetaData("assignmentId");
      } else {
        userSession.addMetaData("assignmentId", assignmentId);
      }
      
      apiManager.setAuthSubToken(authSubToken);

      for (int i = 0; i < videoIds.length(); i++) {
        String videoId = videoIds.getString(i);
        
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
          submission.setPhoneNumber(phoneNumber);
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
            
            // Add video to YouTube playlist if it isn't in it already.
            // This code is kind of ugly and is mostly copy/pasted from UpdateVideoSubmissionStatus
            // TODO: It should be refactored into a common helper method somewhere...
            if (!submission.isInPlaylist()) {
              Assignment assignment = assignmentDao.getAssignmentById(assignmentId);
  
              if (assignment == null) {
                log.warning(String.format("Couldn't find assignment id '%d' for video id '%s'.",
                    assignmentId, videoId));
              } else {
                String playlistId = assignment.getPlaylistId();
                if (util.isNullOrEmpty(playlistId)) {
                  log.warning(String.format("Assignment id '%d' doesn't have an associated playlist.",
                      assignmentId));
                } else {
                  apiManager.setAuthSubToken(adminConfig.getYouTubeAuthSubToken());
                  if (apiManager.insertVideoIntoPlaylist(playlistId, videoId)) {
                    submission.setIsInPlaylist(true);
                  }
                }
              }
            }
          }
  
          pmfUtil.persistJdo(submission);
  
          emailUtil.sendNewSubmissionEmail(submission);
        }
      }
      
      JSONObject responseJsonObj = new JSONObject();
      responseJsonObj.put("success", "true");

      resp.setContentType("text/javascript");
      resp.getWriter().println(responseJsonObj.toString());
    } catch (IllegalArgumentException e) {
      log.log(Level.FINE, "", e);
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
    } catch (JSONException e) {
      log.log(Level.WARNING, "", e);
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }
}