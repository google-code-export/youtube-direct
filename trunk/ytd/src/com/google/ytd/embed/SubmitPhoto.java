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

package com.google.ytd.embed;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.labs.taskqueue.Queue;
import com.google.appengine.api.labs.taskqueue.QueueFactory;
import com.google.appengine.api.labs.taskqueue.TaskOptions.Method;

import static com.google.appengine.api.labs.taskqueue.TaskOptions.Builder.*;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.ytd.dao.AdminConfigDao;
import com.google.ytd.guice.ProductionModule;
import com.google.ytd.model.PhotoEntry;
import com.google.ytd.model.PhotoSubmission;
import com.google.ytd.util.PmfUtil;
import com.google.ytd.util.Util;

/**
 * Servlet that handles the submission of photos. It creates a new
 * PhotoSubmission object, saves it to the datastore, and creates a TaskQueue
 * entry to transmit it to Picasa.
 */
@Singleton
public class SubmitPhoto extends HttpServlet {
  private static final Logger LOG = Logger.getLogger(SubmitPhoto.class.getName());

  private static final long TASK_DELAY = 1000 * 60; // Timeout before task is invoked.

  private Injector injector = null;
  private Util util = null;
  private PmfUtil pmfUtil = null;
  private AdminConfigDao adminConfigDao;

  public SubmitPhoto() {
    injector = Guice.createInjector(new ProductionModule());
    util = injector.getInstance(Util.class);
    pmfUtil = injector.getInstance(PmfUtil.class);
    adminConfigDao = injector.getInstance(AdminConfigDao.class);
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    try {
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

      String articleUrl = req.getParameter("articleUrl");
      if (util.isNullOrEmpty(articleUrl)) {
        throw new IllegalArgumentException("'articleUrl' is null or empty.");
      }

      String email = req.getParameter("uploadEmail");
      if (util.isNullOrEmpty(email)) {
        throw new IllegalArgumentException("'uploadEmail' is null or empty.");
      }

      String author = req.getParameter("author");
      if (util.isNullOrEmpty(author)) {
        throw new IllegalArgumentException("'author' is null or empty.");
      }

      String phoneNumber = req.getParameter("phoneNumber");

      String location = req.getParameter("location");

      String date = req.getParameter("date");

      BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
      Map<String, BlobKey> blobs = blobstoreService.getUploadedBlobs(req);

      BlobInfoFactory blobInfoFactory = new BlobInfoFactory();

      long maxPhotoSize = adminConfigDao.getMaxPhotoSize();

      ArrayList<BlobKey> validSubmissionKeys = new ArrayList<BlobKey>();
      for (Entry<String, BlobKey> entry : blobs.entrySet()) {
        BlobKey blobKey = entry.getValue();

        BlobInfo blobInfo = blobInfoFactory.loadBlobInfo(blobKey);
        String contentType = blobInfo.getContentType().toLowerCase();
        long size = blobInfo.getSize();

        if (!contentType.startsWith("image/")) {
          blobstoreService.delete(blobKey);
          LOG.warning(String.format("Uploaded file has content type '%s'; skipping.", contentType));
          continue;
        }

        if ((size > maxPhotoSize) || (size == 0)) {
          blobstoreService.delete(blobKey);
          LOG.warning(String.format("Uploaded file is %d bytes; skipping.", size));
          continue;
        }

        validSubmissionKeys.add(blobKey);
      }

      if (validSubmissionKeys.size() > 0) {
        // PhotoSubmission represents the metadata of a set of photo entries
        PhotoSubmission photoSubmission =
            new PhotoSubmission(Long.parseLong(assignmentId), articleUrl, author, email,
                phoneNumber, title, description, location, date, validSubmissionKeys.size());
        pmfUtil.persistJdo(photoSubmission);
        String submissionId = photoSubmission.getId();

        Queue queue = QueueFactory.getDefaultQueue();
        queue.add(url("/tasks/MoveToPicasa").method(Method.POST).param("id", submissionId)
            .countdownMillis(TASK_DELAY));

        for (BlobKey blobKey : validSubmissionKeys) {
          BlobInfo blobInfo = blobInfoFactory.loadBlobInfo(blobKey);

          PhotoEntry photoEntry = new PhotoEntry(submissionId, blobKey, blobInfo.getContentType());
          photoEntry.setOriginalFileSize(blobInfo.getSize());
          photoEntry.setOriginalFileName(blobInfo.getFilename());
          
          pmfUtil.persistJdo(photoEntry);
        }
      } else {
        LOG.warning("No valid photos found in upload.");
      }
    } catch (IllegalArgumentException e) {
      LOG.log(Level.WARNING, "", e);
    } finally {
      // As per the BlobStore spec, we need to return a 30x response here. It's
      // ignored by the JS.
      resp.sendRedirect("/blank.html");
    }
  }
}
