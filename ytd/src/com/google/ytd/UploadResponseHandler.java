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
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.ytd.model.AdminConfig;
import com.google.ytd.model.UserSession;
import com.google.ytd.model.VideoSubmission;

/**
 * Servlet that is invoked as part of the browser-based YouTube video upload flow. If it detects
 * a successful upload, it creates a new VideoSubmission object in the datastore that corresponds to
 * the new video, and return a JSON representation of it.
 */
public class UploadResponseHandler extends HttpServlet {
  private static final Logger log = Logger.getLogger(UploadResponseHandler.class.getName());

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String videoId = req.getParameter("id");
    String status = req.getParameter("status");

    UserSession userSession = UserSessionManager.getUserSession(req);

    if (status.equals("200")) {
      String authSubToken = userSession.getMetaData("authSubToken");
      String articleUrl = userSession.getMetaData("articleUrl");
      String assignmentId = userSession.getMetaData("assignmentId");
      String videoTitle = userSession.getMetaData("videoTitle");
      String videoDescription = userSession.getMetaData("videoDescription");
      String youTubeName = userSession.getMetaData("youTubeName");
      String email = userSession.getMetaData("email");
      String videoTags = userSession.getMetaData("videoTags");      
      String videoLocation = userSession.getMetaData("videoLocation");
      String videoDate = userSession.getMetaData("videoDate");

      log.fine(String.format("Attempting to persist VideoSubmission with YouTube id '%s' "
          + "for assignment id '%s'...", videoId, assignmentId));
      
      VideoSubmission submission = new VideoSubmission(Long.parseLong(assignmentId));
      
      submission.setArticleUrl(articleUrl);
      submission.setVideoId(videoId);
      submission.setVideoTitle(videoTitle);
      submission.setVideoDescription(videoDescription);
      submission.setVideoTags(videoTags);
      submission.setVideoLocation(videoLocation);      
      submission.setVideoDate(videoDate);      
      submission.setYouTubeName(youTubeName);
      // Note: the call to setAuthSubToken needs to be made after the call to setYouTubeName,
      // since setAuthSubToken relies on a youtubeName being set in order to proxy to the
      // UserAuthToken class.
      submission.setAuthSubToken(authSubToken);
      submission.setVideoSource(VideoSubmission.VideoSource.NEW_UPLOAD);      
      submission.setNotifyEmail(email);
      
      AdminConfig adminConfig = Util.getAdminConfig();
      
      YouTubeApiManager apiManager = new YouTubeApiManager();
      apiManager.setToken(adminConfig.getYouTubeAuthSubToken());
      
      if (adminConfig.getModerationMode() == AdminConfig.ModerationModeType.NO_MOD.ordinal()) {
        // NO_MOD is set, auto approve all submission
        //TODO: This isn't enough, as the normal approval flow (adding the branding, tags, emails,
        // etc.) isn't taking place.
        submission.setStatus(VideoSubmission.ModerationStatus.APPROVED);
        apiManager.updateModeration(videoId, true);
      } else {
        apiManager.updateModeration(videoId, false);
      }
      
      Util.persistJdo(submission);
      
      log.fine("...VideoSubmission persisted.");

      Util.sendNewSubmissionEmail(submission);
      
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