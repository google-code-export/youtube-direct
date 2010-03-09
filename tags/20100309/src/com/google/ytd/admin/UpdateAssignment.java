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
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonParseException;
import com.google.ytd.Util;
import com.google.ytd.YouTubeApiManager;
import com.google.ytd.model.AdminConfig;
import com.google.ytd.model.Assignment;
import com.google.ytd.model.Assignment.AssignmentStatus;

/**
 * Servlet responsible for updating an Assignment in the datastore.
 */
public class UpdateAssignment extends HttpServlet {
  private static final Logger log = Logger.getLogger(UpdateAssignment.class.getName());

  @SuppressWarnings("cast")
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    PersistenceManager pm = Util.getPersistenceManagerFactory().getPersistenceManager();

    try {
      String json = Util.getPostBody(req);
      if (Util.isNullOrEmpty(json)) {
        throw new IllegalArgumentException("No JSON data found in HTTP POST request.");
      }

      Assignment incomingEntry = Util.GSON.fromJson(json, Assignment.class);
      long id = incomingEntry.getId();
      Assignment assignment = (Assignment) pm.getObjectById(Assignment.class, id);
      
      // If the updated assignment's status is ACTIVE and the existing assignment doesn't already
      // have a playlist, create one.
      if (incomingEntry.getStatus() == AssignmentStatus.ACTIVE &&
              Util.isNullOrEmpty(assignment.getPlaylistId())) {
        YouTubeApiManager apiManager = new YouTubeApiManager();
        apiManager.setRequestIpAddress(req.getRemoteAddr());
        
        AdminConfig adminConfig = Util.getAdminConfig();
        String token = adminConfig.getYouTubeAuthSubToken();
        if (Util.isNullOrEmpty(token)) {
          log.warning(String.format("Could not create new playlist for assignment '%s' because no" +
              " YouTube AuthSub token was found in the config.", assignment.getDescription()));
        } else {
          apiManager.setToken(token);
          String playlistId = apiManager.createPlaylist(String.format("Playlist for Assignment #%d",
                  assignment.getId()), assignment.getDescription());
          
          assignment.setPlaylistId(playlistId);
        }
      }
      
      assignment.setStatus(incomingEntry.getStatus());
      assignment.setDescription(incomingEntry.getDescription());
      assignment.setCategory(incomingEntry.getCategory());
      
      pm.makePersistent(assignment);
      
      log.info(String.format("Updated assignment id %d in the datastore.", assignment.getId()));

      resp.setContentType("text/javascript");
      resp.getWriter().println(Util.GSON.toJson(assignment));
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
