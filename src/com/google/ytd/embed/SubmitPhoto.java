/* Copyright (c) 2010 Google Inc.
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
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.inject.Singleton;
import com.google.ytd.model.PhotoEntry;
import com.google.ytd.model.PhotoSubmission;
import com.google.ytd.util.PmfUtil;
import com.google.ytd.util.Util;

/**
 * Servlet that handles the submission of photos. It creates a new
 * PhotoSubmission object and saves it to the datastore. The response needs to
 * be a 30x redirect, as per the BlobStore API.
 */
@Singleton
public class SubmitPhoto extends HttpServlet {
  private static final Logger log = Logger.getLogger(SubmitPhoto.class.getName());

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    try {
      Util util = Util.get();

      // Create a new PMF because unfortunately we don't have access to the Guice version.
      PersistenceManagerFactory pmf = JDOHelper
          .getPersistenceManagerFactory("transactions-optional");
      PmfUtil pmfUtil = new PmfUtil(pmf);

      String assignmentId = req.getParameter("assignmentId");
      if (util.isNullOrEmpty(assignmentId)) {
        throw new IllegalArgumentException("'assignmentId' is null or empty.");
      }

      String title = req.getParameter("title");
      if (util.isNullOrEmpty(title)) {
        throw new IllegalArgumentException("'title' is null or empty.");
      }

      String description = req.getParameter("description");
      if (util.isNullOrEmpty(description)) {
        throw new IllegalArgumentException("'description' is null or empty.");
      }

      String location = req.getParameter("location");

      String email = req.getParameter("uploadEmail");
      if (util.isNullOrEmpty(email)) {
        throw new IllegalArgumentException("'uploadEmail' is null or empty.");
      }

      BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
      Map<String, BlobKey> blobs = blobstoreService.getUploadedBlobs(req);

      // PhotoSubmission represents the meta data of a set of photo entries     
      PhotoSubmission photoSubmission = new PhotoSubmission(Long.parseLong(assignmentId), email,
          title, description, location, blobs.entrySet().size());
      pmfUtil.persistJdo(photoSubmission);
      String submissionId = photoSubmission.getId();

      for (Entry<String, BlobKey> entry : blobs.entrySet()) {
        log.info(String.format("Processing file form element '%s'.", entry.getKey()));
        BlobKey blobKey = entry.getValue();
        PhotoEntry photo = new PhotoEntry(submissionId, blobKey);
        pmfUtil.persistJdo(photo);
      }
    } catch (IllegalArgumentException e) {
      log.log(Level.WARNING, "", e);
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
    } finally {

    }
  }
}