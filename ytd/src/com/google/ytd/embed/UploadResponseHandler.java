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
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.ytd.dao.AdminConfigDao;
import com.google.ytd.dao.AssignmentDao;
import com.google.ytd.dao.UserAuthTokenDao;
import com.google.ytd.dao.VideoSubmissionDao;
import com.google.ytd.model.AdminConfig;
import com.google.ytd.model.Assignment;
import com.google.ytd.model.UserSession;
import com.google.ytd.model.VideoSubmission;
import com.google.ytd.util.EmailUtil;
import com.google.ytd.util.Util;
import com.google.ytd.youtube.YouTubeApiHelper;

/**
 * Servlet that is invoked as part of the browser-based YouTube video upload
 * flow. If it detects a successful upload, it creates a new VideoSubmission
 * object in the datastore that corresponds to the new video, and return a JSON
 * representation of it.
 */
@Singleton
public class UploadResponseHandler extends HttpServlet {
  private static final Logger log = Logger.getLogger(UploadResponseHandler.class.getName());

  @Inject
  private EmailUtil emailUtil;
  @Inject
  private Util util;
  @Inject
  private UserSessionManager userSessionManager;
  @Inject
  private YouTubeApiHelper youTubeApiHelper;
  @Inject
  private VideoSubmissionDao submissionDao;
  @Inject
  private UserAuthTokenDao userAuthTokenDao;
  @Inject
  private AdminConfigDao adminConfigDao;
  @Inject
  private AssignmentDao assignmentDao;

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String videoId = req.getParameter("id");
    String status = req.getParameter("status");

    UserSession userSession = userSessionManager.getUserSession(req);

    if (status.equals("200")) {
      String authSubToken = userSession.getMetaData("authSubToken");
      String articleUrl = userSession.getMetaData("articleUrl");
      String assignmentId = userSession.getMetaData("assignmentId");
      String videoTitle = userSession.getMetaData("videoTitle");
      String videoDescription = userSession.getMetaData("videoDescription");
      String youTubeName = userSession.getMetaData("youTubeName");
      String email = userSession.getMetaData("email");
      String phoneNumber = userSession.getMetaData("phoneNumber");
      String videoTags = userSession.getMetaData("videoTags");
      String videoLocation = userSession.getMetaData("videoLocation");
      String videoDate = userSession.getMetaData("videoDate");

      log.fine(String.format("Attempting to persist VideoSubmission with YouTube id '%s' "
          + "for assignment id '%s'...", videoId, assignmentId));

      VideoSubmission submission = submissionDao.newSubmission(Long.parseLong(assignmentId));

      submission.setArticleUrl(articleUrl);
      submission.setVideoId(videoId);
      submission.setVideoTitle(videoTitle);
      submission.setVideoDescription(videoDescription);
      submission.setVideoTags(videoTags);
      submission.setVideoLocation(videoLocation);
      submission.setVideoDate(videoDate);
      submission.setYouTubeName(youTubeName);
      submission.setVideoSource(VideoSubmission.VideoSource.NEW_UPLOAD);
      submission.setNotifyEmail(email);
      submission.setPhoneNumber(phoneNumber);

      userAuthTokenDao.setUserAuthToken(youTubeName, authSubToken);

      AdminConfig adminConfig = adminConfigDao.getAdminConfig();

      youTubeApiHelper.setAuthSubToken(adminConfig.getYouTubeAuthSubToken());

      if (adminConfig.getModerationMode() == AdminConfig.ModerationModeType.NO_MOD.ordinal()) {
        // NO_MOD is set, auto approve all submission
        // TODO: This isn't enough, as the normal approval flow (adding the branding, tags, emails,
        // etc.) isn't taking place.
        submission.setStatus(VideoSubmission.ModerationStatus.APPROVED);
        youTubeApiHelper.updateModeration(videoId, true);
        
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
              log.warning(String.format("Assignment id '%d' does not have an associated playlist.",
                  assignmentId));
            } else {
              if (youTubeApiHelper.insertVideoIntoPlaylist(playlistId, videoId)) {
                submission.setIsInPlaylist(true);
              }
            }
          }
        }
      } else {
        youTubeApiHelper.updateModeration(videoId, false);
      }

      submission = submissionDao.save(submission);

      log.fine("...VideoSubmission persisted.");

      emailUtil.sendNewSubmissionEmail(submission);

      try {
        JSONObject responseJsonObj = new JSONObject();
        responseJsonObj.put("videoId", videoId);
        responseJsonObj.put("status", status);
        resp.setContentType("text/html");
        resp.getWriter().println(responseJsonObj.toString());
      } catch (JSONException e) {
        log.warning(e.toString());
        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
      }
    } else {
      String code = req.getParameter("code");
      log.warning(String.format("Upload request for user with session id '%s' failed with "
          + "status '%s' and code '%s'.", userSession.getId(), status, code));
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, code);
    }
  }
}