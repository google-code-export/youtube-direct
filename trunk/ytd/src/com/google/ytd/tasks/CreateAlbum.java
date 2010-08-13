/*
 * Copyright (c) 2010 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.ytd.tasks;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.ytd.dao.AssignmentDao;
import com.google.ytd.model.Assignment;
import com.google.ytd.model.PhotoEntry.ModerationStatus;
import com.google.ytd.picasa.PicasaApiHelper;
import com.google.ytd.util.Util;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Singleton
public class CreateAlbum extends HttpServlet {
  private static final Logger LOG = Logger.getLogger(CreateAlbum.class.getName());

  @Inject
  private Util util;
  @Inject
  private AssignmentDao assignmentDao;
  @Inject
  private PicasaApiHelper picasaApi;

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    LOG.info("Starting up...");

    try {
      if (!picasaApi.isAuthenticated()) {
        throw new IllegalArgumentException("No Picasa AuthSub token found in the configuration.");
      }
      
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
      
      String status = request.getParameter("status");
      if (util.isNullOrEmpty(status)) {
        throw new IllegalArgumentException("Required parameter 'status' is null or empty.");
      }
      
      Assignment assignment = assignmentDao.getAssignmentById(assignmentId);
      if (assignment == null) {
        throw new IllegalArgumentException(String.format("Could not find Assignment id '%s' in "
            + "datastore.", assignmentId));
      }
      
      String albumUrl;
      ModerationStatus statusEnum = ModerationStatus.valueOf(status);
      switch (statusEnum) {
        case APPROVED:
          albumUrl = picasaApi.createAlbum(title, description, false);
          if (util.isNullOrEmpty(albumUrl)) {
            throw new IllegalStateException("Unable to create 'APPROVED' album.");
          } else {
            assignment.setApprovedAlbumUrl(albumUrl);
          }
          break;
          
        case UNREVIEWED:
          albumUrl = picasaApi.createAlbum(title + " (Unreviewed)", description, true);
          if (util.isNullOrEmpty(albumUrl)) {
            throw new IllegalStateException("Unable to create 'UNREVIEWED' album.");
          } else {
            assignment.setUnreviewedAlbumUrl(albumUrl);
          }
          break;
          
        case REJECTED:
          albumUrl = picasaApi.createAlbum(title + " (Rejected)", description, true);
          if (util.isNullOrEmpty(albumUrl)) {
            throw new IllegalStateException("Unable to create 'REJECTED' album.");
          } else {
            assignment.setRejectedAlbumUrl(albumUrl);
          }
          break;
          
        default:
          throw new IllegalArgumentException(String.format("'%s' is not a valid value for the "
              + "'status' parameter.", status));
      }
      
      assignmentDao.save(assignment);
    } catch (IllegalArgumentException e) {
      // We don't want to send an error response here, since that will result in the
      // TaskQueue retrying and this is not a transient error.
      LOG.log(Level.WARNING, "", e);
    } catch (IllegalStateException e) {
      LOG.log(Level.WARNING, "", e);
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }
}
