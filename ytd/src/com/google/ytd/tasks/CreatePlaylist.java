/*
 * Copyright (c) 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.ytd.tasks;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.ytd.dao.AdminConfigDao;
import com.google.ytd.dao.AssignmentDao;
import com.google.ytd.model.Assignment;
import com.google.ytd.util.Util;
import com.google.ytd.youtube.YouTubeApiHelper;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Singleton
public class CreatePlaylist extends HttpServlet {
  private static final Logger LOG = Logger.getLogger(CreatePlaylist.class.getName());

  @Inject
  private Util util;
  @Inject
  private AssignmentDao assignmentDao;
  @Inject
  private YouTubeApiHelper youtubeApi;
  @Inject
  private AdminConfigDao adminConfigDao;

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    LOG.info("Starting up...");

    try {
      String assignmentId = request.getParameter("assignmentId");
      if (util.isNullOrEmpty(assignmentId)) {
        throw new IllegalArgumentException("Required parameter 'assignmentId' is null or empty.");
      }

      String title = request.getParameter("title");
      if (util.isNullOrEmpty(title)) {
        throw new IllegalArgumentException("Required parameter 'title' is null or empty.");
      }

      String description = request.getParameter("description");
      if (util.isNullOrEmpty(description)) {
        throw new IllegalArgumentException("Required parameter 'description' is null or empty.");
      }

      Assignment assignment = assignmentDao.getAssignmentById(assignmentId);
      if (assignment == null) {
        throw new IllegalArgumentException(
            String.format("Could not find Assignment id '%s' in datastore.", assignmentId));
      }

      String token = adminConfigDao.getAdminConfig().getYouTubeAuthSubToken();
      if (util.isNullOrEmpty(token)) {
        throw new IllegalArgumentException(
            String.format("Could not create new playlist for assignment '%s' because no YouTube "
                + "AuthSub token was found in the config.", assignmentId));
      }
      youtubeApi.setAuthSubToken(token);

      if (util.isNullOrEmpty(adminConfigDao.getAdminConfig().getDeveloperKey())) {
        throw new IllegalArgumentException(
            String.format("Could not create new playlist for assignment '%s' because no YouTube "
                + "developer key was found in the config.", assignmentId));
      }

      String playlistId = youtubeApi.createPlaylist(title, description);
      if (util.isNullOrEmpty(playlistId)) {
        throw new IllegalStateException("Unable to create playlist.");
      } else {
        assignment.setPlaylistId(playlistId);
      }

      assignmentDao.save(assignment);
    } catch (IllegalArgumentException e) {
      // We don't want to send an error response here, since that will result in
      // the TaskQueue retrying and this is not a transient error.
      LOG.log(Level.WARNING, "", e);
    } catch (IllegalStateException e) {
      LOG.log(Level.WARNING, "", e);
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }
}
