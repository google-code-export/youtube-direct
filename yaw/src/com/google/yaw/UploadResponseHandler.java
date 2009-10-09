package com.google.yaw;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.yaw.model.AdminConfig;
import com.google.yaw.model.Assignment;
import com.google.yaw.model.UserSession;
import com.google.yaw.model.VideoSubmission;

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