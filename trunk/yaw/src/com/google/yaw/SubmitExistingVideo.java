package com.google.yaw;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gdata.data.media.mediarss.MediaCategory;
import com.google.gdata.data.media.mediarss.MediaDescription;
import com.google.gdata.data.media.mediarss.MediaKeywords;
import com.google.gdata.data.media.mediarss.MediaTitle;
import com.google.gdata.data.youtube.FormUploadToken;
import com.google.gdata.data.youtube.VideoEntry;
import com.google.gdata.data.youtube.YouTubeMediaGroup;
import com.google.gdata.data.youtube.YouTubeNamespace;
import com.google.gdata.data.youtube.YtStatistics;
import com.google.yaw.model.AdminConfig;
import com.google.yaw.model.Assignment;
import com.google.yaw.model.UserSession;
import com.google.yaw.model.VideoSubmission;
import com.google.yaw.model.Assignment.AssignmentStatus;

public class SubmitExistingVideo extends HttpServlet {

  private static final Logger log = Logger.getLogger(SubmitExistingVideo.class.getName());

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String json = Util.getPostBody(req);
    
    try {
      JSONObject jsonObj = new JSONObject(json);
      
      String videoId = jsonObj.getString("videoId");
      String location = jsonObj.getString("location");
      String date = jsonObj.getString("date");
      String email = jsonObj.getString("email");                        
      
      // Only check for required parameters 'videoId'.
      if (Util.isNullOrEmpty(videoId)) {
        throw new IllegalArgumentException("'videoId' parameter is null or empty.");
      }
      
      // Grab user session meta data
      UserSession userSession = UserSessionManager.getUserSession(req);
      String youTubeName = userSession.getMetaData("youTubeName");
      String authSubToken = userSession.getMetaData("authSubToken");
      String assignmentId = userSession.getMetaData("assignmentId");
      String articleUrl = userSession.getMetaData("articleUrl");
      
      YouTubeApiManager apiManager = new YouTubeApiManager();      
      apiManager.setToken(authSubToken);
      
      VideoEntry videoEntry = apiManager.getUploadsVideoEntry(videoId);      
      
      if (videoEntry == null) {        
        log.info("videoEntry is null");
        JSONObject responseJsonObj = new JSONObject();
        responseJsonObj.put("success", "false");
        responseJsonObj.put("message", "Cannot find this video in your account.");  

        resp.setContentType("text/javascript");
        resp.getWriter().println(responseJsonObj.toString());
      } else {      
        log.info("videoEntry is NOT null");
        String title = videoEntry.getTitle().getPlainText();
        String description = videoEntry.getMediaGroup().getDescription().getPlainTextContent();
        
        List<String> tags = videoEntry.getMediaGroup().getKeywords().getKeywords();
        String sortedTags = Util.sortedJoin(tags, ",");
        
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
        // Note: the call to setAuthSubToken needs to be made after the call to setYouTubeName,
        // since setAuthSubToken relies on a youtubeName being set in order to proxy to the
        // UserAuthToken class.
        submission.setAuthSubToken(authSubToken);
        submission.setVideoSource(VideoSubmission.VideoSource.EXISTING_VIDEO);      
        submission.setNotifyEmail(email);

        AdminConfig adminConfig = Util.getAdminConfig();      
        if (adminConfig.getModerationMode() == AdminConfig.ModerationModeType.NO_MOD.ordinal()) {
          // NO_MOD is set, auto approve all submission
          //TODO: This isn't enough, as the normal approval flow (adding the branding, tags, emails,
          // etc.) isn't taking place.
          submission.setStatus(VideoSubmission.ModerationStatus.APPROVED);        
        }
        
        Util.persistJdo(submission);
        
        Util.sendNewSubmissionEmail(submission);
        
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
    }
  }
}