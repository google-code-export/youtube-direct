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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManagerFactory;
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
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.ytd.dao.AssignmentDao;
import com.google.ytd.model.Assignment;
import com.google.ytd.model.UserSession;
import com.google.ytd.model.Assignment.AssignmentStatus;
import com.google.ytd.util.Util;
import com.google.ytd.youtube.YouTubeApiHelper;

/**
 * Class responsible for submitting metadata about a new video to the YouTube API server. It gets
 * back a unique token that can be used to upload a video. This token is returned as part of a
 * JSON response.
 */
@Singleton
public class GetUploadToken extends HttpServlet {
  private static final Logger log = Logger.getLogger(GetUploadToken.class.getName());

  @Inject
  private Util util;
  @Inject
  private PersistenceManagerFactory pmf;
  @Inject
  private UserSessionManager userSessionManager;
  @Inject
  private YouTubeApiHelper apiManager;
  @Inject
  private AssignmentDao assignmentDao;

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String json = util.getPostBody(req);

    try {
      JSONObject jsonObj = new JSONObject(json);

      String title = jsonObj.getString("title");
      String description = jsonObj.getString("description");
      String location = jsonObj.getString("location");
      String date = jsonObj.getString("date");
      String email = jsonObj.getString("email");
      JSONArray tagsArray = jsonObj.getJSONArray("tags");

      // Only check for required parameters 'title' and 'description'.
      if (util.isNullOrEmpty(title)) {
        throw new IllegalArgumentException("'title' parameter is null or empty.");
      }
      if (util.isNullOrEmpty(description)) {
        throw new IllegalArgumentException("'description' parameter is null or empty.");
      }

      UserSession userSession = userSessionManager.getUserSession(req);
      if (userSession == null) {
        // TODO: Throw a better Exception class here.
        throw new IllegalArgumentException("No user session found.");
      }
      String authSubToken = userSession.getMetaData("authSubToken");
      String assignmentId = userSession.getMetaData("assignmentId");

      Assignment assignment = assignmentDao.getAssignmentById(assignmentId);
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
      String sortedTags = util.sortedJoin(tags, ",");

      mg.setDescription(new MediaDescription());
      mg.getDescription().setPlainTextContent(description);

      // TODO: Move this to a config or constant.
      mg.addCategory(new MediaCategory(YouTubeNamespace.DEVELOPER_TAG_SCHEME, "ytd"));

      // Maximum size of a developer tag is 25 characters, and we prepend 2 characters.
      if (assignmentId.length() <= 23) {
        // Minimum size of a developer tag is 3 characters, so always append 2 characters.
        String assignmentIdTag = String.format("A-%s", assignmentId);

        // Use a developer tag to make it easy for developers to query for all videos uploaded for
        // a given assignment.
        mg.addCategory(new MediaCategory(YouTubeNamespace.DEVELOPER_TAG_SCHEME, assignmentIdTag));
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
      userSessionManager.save(userSession);

      apiManager.setToken(authSubToken);

      FormUploadToken token = apiManager.getFormUploadToken(newEntry);
      if (token == null) {
        throw new IllegalArgumentException("Upload token returned from YouTube API is null. " +
        		"Please make sure that all request parameters are valid.");
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