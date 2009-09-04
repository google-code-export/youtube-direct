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
import com.google.yaw.model.Assignment;
import com.google.yaw.model.UserSession;
import com.google.yaw.model.Assignment.AssignmentStatus;

public class GetUploadToken extends HttpServlet {

  private static final Logger log = Logger.getLogger(GetUploadToken.class.getName());

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String json = Util.getPostBody(req);
    
    try {
      JSONObject jsonObj = new JSONObject(json);

      String title = jsonObj.getString("title");
      String description = jsonObj.getString("description");
      String location = jsonObj.getString("location");
      String date = jsonObj.getString("date");
      String email = jsonObj.getString("email");
      JSONArray tagsArray = jsonObj.getJSONArray("tags");

      // Only check for required parameters 'title' and 'description'.
      if (Util.isNullOrEmpty(title)) {
        throw new IllegalArgumentException("'title' parameter is null or empty.");
      }
      if (Util.isNullOrEmpty(description)) {
        throw new IllegalArgumentException("'description' parameter is null or empty.");
      }

      UserSession userSession = UserSessionManager.getUserSession(req);
      if (userSession == null) {
        // TODO: Throw a better Exception class here.
        throw new IllegalArgumentException("No user session found.");
      }
      String authSubToken = userSession.getMetaData("authSubToken");
      String assignmentId = userSession.getMetaData("assignmentId");            

      Assignment assignment = Util.getAssignmentById(assignmentId);
      if (assignment == null) {
        throw new IllegalArgumentException(String.format(
            "Could not find an assignment with id '%s'.", assignmentId));
      }
      AssignmentStatus status = assignment.getStatus();
      if (status != AssignmentStatus.ACTIVE) {
        throw new IllegalArgumentException(String.format(
            "Could not add a video to a non ACTIVE assignment. "
                + "Current status of assignment id '%s' is '%s'.", assignmentId, status));
      }

      // Max title length is 60 characters or 100 bytes.
      if (title.length() > 60) {
        title = title.substring(0, 59);
      }

      VideoEntry newEntry = new VideoEntry();
      YouTubeMediaGroup mg = newEntry.getOrCreateMediaGroup();

      mg.setTitle(new MediaTitle());
      mg.getTitle().setPlainTextContent(title);

      mg.addCategory(new MediaCategory(YouTubeNamespace.CATEGORY_SCHEME, assignment.getCategory()));

      mg.setKeywords(new MediaKeywords());

      List<String> tags = new ArrayList<String>();
      for (int i = 0; i < tagsArray.length(); i++) {
        String tag = tagsArray.getString(i).trim();
        mg.getKeywords().addKeyword(tag);
        tags.add(tag);
      }

      // Sort the list of tags and join with "," so that we can easily compare
      // what's in the
      // datastore with what we get back from the YouTube API.
      String sortedTags = Util.sortedJoin(tags, ",");

      mg.setDescription(new MediaDescription());
      mg.getDescription().setPlainTextContent(description);

      String defaultDeveloperTag = System.getProperty("com.google.yaw.DefaultDeveloperTag");
      if (!Util.isNullOrEmpty(defaultDeveloperTag)) {
        mg.addCategory(new MediaCategory(YouTubeNamespace.DEVELOPER_TAG_SCHEME,
                defaultDeveloperTag));
      }
      
      // Maximum size of a developer tag is 25 characters.
      if (assignmentId.length() <= 25) {
        // Use the assignmentId as a developer tag to make it easy for developers to query for all
        // videos uploaded for a given assignment.
        mg.addCategory(new MediaCategory(YouTubeNamespace.DEVELOPER_TAG_SCHEME, assignmentId));
      } else {
        log.warning(String.format("Assignment id '%s' is longer than 25 characters, and can't be " +
                "used as a developer tag.", assignmentId));
      }

      userSession.addMetaData("videoTitle", title);      
      userSession.addMetaData("videoDescription", description);
      userSession.addMetaData("videoLocation", location);
      userSession.addMetaData("videoDate", date);         
      userSession.addMetaData("videoTags", sortedTags);
      userSession.addMetaData("email", email);
      UserSessionManager.save(userSession);

      YouTubeApiManager apiManager = new YouTubeApiManager();
      apiManager.setToken(authSubToken);

      FormUploadToken token = apiManager.getFormUploadToken(newEntry);
      if (token == null) {
        throw new IllegalArgumentException("Upload token returned from YouTube API is null.");
      }

      String uploadToken = token.getToken();
      String uploadUrl = token.getUrl();

      JSONObject responseJsonObj = new JSONObject();
      responseJsonObj.put("uploadToken", uploadToken);
      responseJsonObj.put("uploadUrl", uploadUrl);

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