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

import javax.jdo.PersistenceManagerFactory;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gdata.data.Link;
import com.google.gdata.data.youtube.VideoEntry;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.ytd.util.Util;
import com.google.ytd.youtube.YouTubeApiHelper;

/**
 * Servlet that retrieves the Insight download link for a video and redirects
 * the browser there.
 * 
 * The Insight download link is generated with a time-sensitive parameter, so it
 * can't be cached. More info on Insight data is available at
 * http://code.google.
 * com/apis/youtube/2.0/developers_guide_protocol_insight.html
 */
@Singleton
public class InsightDownloadRedirect extends HttpServlet {
  private static final Logger log = Logger.getLogger(InsightDownloadRedirect.class.getName());

  @Inject
  private Util util;
  @Inject
  private PersistenceManagerFactory pmf;
  @Inject
  private YouTubeApiHelper apiManager;

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    try {
      String id = req.getParameter("id");
      if (util.isNullOrEmpty(id)) {
        throw new IllegalArgumentException("'id' parameter is null or empty.");
      }

      String token = req.getParameter("token");
      if (util.isNullOrEmpty(token)) {
        throw new IllegalArgumentException("'token' parameter is null or empty.");
      }

      apiManager.setToken(token);

      VideoEntry videoEntry = apiManager.getVideoEntry(id);
      if (videoEntry == null) {
        throw new IllegalArgumentException(String.format("Couldn't retrieve video entry with id "
            + "'%s' using token '%s'.", id, token));
      }

      Link insightLink = videoEntry.getLink("http://gdata.youtube.com/schemas/2007#insight.views",
          null);
      if (insightLink != null) {
        String url = insightLink.getHref();
        if (util.isNullOrEmpty(url)) {
          throw new IllegalArgumentException(String.format("No insight download URL found for "
              + "video id '%s'.", id));
        }

        resp.sendRedirect(url);
      } else {
        throw new IllegalArgumentException(String.format("No insight download link found for "
            + "video id '%s'.", id));
      }
    } catch (IllegalArgumentException e) {
      log.log(Level.WARNING, "", e);
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
    }
  }
}