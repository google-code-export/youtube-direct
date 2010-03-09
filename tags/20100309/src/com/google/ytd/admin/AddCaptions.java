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

import com.google.ytd.Util;
import com.google.ytd.YouTubeApiManager;
import com.google.ytd.model.VideoSubmission;

/**
 * Servlet that deletes VideoSubmission objects from the datastore.
 * 
 * The underlying YouTube video is not modified.
 */
public class AddCaptions extends HttpServlet {

  private static final Logger log = Logger.getLogger(AddCaptions.class.getName());

  @Override
  @SuppressWarnings("cast")
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    PersistenceManagerFactory pmf = Util.getPersistenceManagerFactory();
    PersistenceManager pm = pmf.getPersistenceManager();

    try {
      String id = req.getParameter("id");
      String captionText = req.getParameter("captionText");
      String captionLanguage = req.getParameter("captionLanguage");
      
      if (Util.isNullOrEmpty(captionText)) {
        throw new IllegalArgumentException("'captionText' parameter is null or empty.");
      }
      
      if (Util.isNullOrEmpty(captionLanguage)) {
        throw new IllegalArgumentException("'captionLanguage' parameter is null or empty.");
      }
      
      if (Util.isNullOrEmpty(id)) {
        throw new IllegalArgumentException("'id' parameter is null or empty.");
      }
      
      VideoSubmission videoSubmission = (VideoSubmission)pm.getObjectById(
          VideoSubmission.class, id);

      YouTubeApiManager apiManager = new YouTubeApiManager();
      apiManager.setToken(videoSubmission.getAuthSubToken());
      apiManager.setHeader("Content-Language", captionLanguage);
      apiManager.setHeader("Content-Type", "application/vnd.youtube.timedtext; charset=UTF-8");
      
      boolean success = apiManager.addCaptions(videoSubmission.getVideoId(), captionText);
      
      resp.setContentType("text/plain");
      resp.getWriter().println(success);
    } catch (IllegalArgumentException e) {
      log.log(Level.WARNING, "", e);
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
    } finally {
      pm.close();
    }
  }
}
