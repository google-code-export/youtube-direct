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

package com.google.ytd.admin;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonParseException;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.ytd.YouTubeApiManager;
import com.google.ytd.model.AdminConfig;
import com.google.ytd.model.Assignment;
import com.google.ytd.model.Assignment.AssignmentStatus;
import com.google.ytd.util.Util;

/**
 * Servlet responsible for creating new a new Assignment in the datastore and returning a JSON
 * representation of it.
 */
@Singleton
public class NewAssignment extends HttpServlet {
  private static final Logger log = Logger.getLogger(NewAssignment.class.getName());
  @Inject
  private Util util;
  @Inject
  private PersistenceManagerFactory pmf;
  @Inject
  private YouTubeApiManager apiManager;

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    PersistenceManager pm = pmf.getPersistenceManager();

    try {
      String json = util.getPostBody(req);
      if (util.isNullOrEmpty(json)) {
        throw new IllegalArgumentException("No JSON data found in HTTP POST request.");
      }

      AdminConfig adminConfig = util.getAdminConfig();
      if (util.isNullOrEmpty(adminConfig.getClientId()) ||
              util.isNullOrEmpty(adminConfig.getDeveloperKey()) ||
              util.isNullOrEmpty(adminConfig.getYouTubeAuthSubToken())) {
        throw new IllegalArgumentException("Unable to create new assignment. " +
        		"Please configure all YouTube API settings first.");
      }

      Assignment jsonObj = util.GSON.fromJson(json, Assignment.class);

      Assignment assignment = new Assignment();
      assignment.setStatus(jsonObj.getStatus());
      assignment.setDescription(jsonObj.getDescription());
      assignment.setCategory(jsonObj.getCategory());

      // Need to make it persistant first in order to get an id assigned to it.
      assignment = pm.makePersistent(assignment);

      if (assignment.getStatus() == AssignmentStatus.ACTIVE &&
              util.isNullOrEmpty(assignment.getPlaylistId())) {

        String token = adminConfig.getYouTubeAuthSubToken();
        apiManager.setToken(token);
        String playlistId = apiManager.createPlaylist(String.format("Playlist for Assignment #%d",
                assignment.getId()), assignment.getDescription());
        assignment.setPlaylistId(playlistId);

        // Persist again with the updated playlist id.
        pm.makePersistent(assignment);
      }

      log.info(String.format("Added assignment id %d to the datastore.", assignment.getId()));

      resp.setContentType("text/javascript");
      resp.getWriter().println(util.toJson(assignment));
    } catch (JsonParseException e) {
      log.log(Level.WARNING, "", e);
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
    } catch (IllegalArgumentException e) {
      log.log(Level.WARNING, "", e);
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
    } finally {
      pm.close();
    }
  }

}
