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

import com.google.gdata.data.youtube.VideoEntry;
import com.google.gdata.data.youtube.YouTubeMediaRating;
import com.google.gdata.data.youtube.YtPublicationState;
import com.google.gdata.data.youtube.YtStatistics;
import com.google.ytd.Util;
import com.google.ytd.YouTubeApiManager;
import com.google.ytd.model.VideoSubmission;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet that syncs metadata for a YouTube with the local datastore.
 */
public class SyncMetadata extends HttpServlet {
  private static final Logger log = Logger.getLogger(SyncMetadata.class.getName());
  
  private static final long REFRESH_INTERVAL = 6 * 60 * 60 * 1000; // 6 hours, in milliseconds

  @SuppressWarnings("unchecked")
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    PersistenceManager pm = Util.getPersistenceManagerFactory().getPersistenceManager();

    try {
      String submissionId = Util.getPostBody(req);
      if (Util.isNullOrEmpty(submissionId)) {
        throw new IllegalArgumentException("No submission id found in POST body.");
      }
      
      submissionId = submissionId.trim();
      
      log.info(String.format("Sync request for submission id %s", submissionId));
      
      VideoSubmission videoSubmission = pm.getObjectById(VideoSubmission.class, submissionId);
      if (videoSubmission == null) {
        throw new IllegalArgumentException(String.format("Unable to retrieve submission with id " +
        		"%s from datastore."));
      }
      
      Date now = new Date();
      long delta = now.getTime() - videoSubmission.getLastSynced().getTime();
      if (delta > REFRESH_INTERVAL) {
        YouTubeApiManager apiManager = new YouTubeApiManager();
        apiManager.setRequestIpAddress(req.getRemoteAddr());
        apiManager.setToken(videoSubmission.getAuthSubToken());
       
        String videoId = videoSubmission.getVideoId();

        // This will retrieve video info from the Uploads feed of the user who owns the video.
        // This should always be the freshest data, but it relies on the AuthSub token being valid.
        VideoEntry videoEntry = apiManager.getUploadsVideoEntry(videoId);
        if (videoEntry == null) {
          // Try an unauthenticated request to the specific user's uploads feed next.
          apiManager = new YouTubeApiManager();
          apiManager.setRequestIpAddress(req.getRemoteAddr());
          videoEntry = apiManager.getUploadsVideoEntry(videoSubmission.getYouTubeName(), videoId);
         
          if (videoEntry == null) {
            // Fall back on looking for the video in the public feed.
            apiManager = new YouTubeApiManager();
            apiManager.setRequestIpAddress(req.getRemoteAddr());
            videoEntry = apiManager.getVideoEntry(videoId);
           
            if (videoEntry == null) {
              // The video must have been deleted...
              log.info(String.format("Unable to find YouTube video id '%s'.", videoId));
              videoSubmission.setYoutubeState("NOT_FOUND");
            }
          }
        }
       
        if (videoEntry != null) {
          try {
            YtPublicationState state = videoEntry.getPublicationState();
            String stateValue;
            if (state == null) {
              // TODO: Find some way to check whether the video is embeddable and/or private, and
              // populate that info. Because we're getting the video from the authenticated
              // uploads feed (by default), that info isn't easily exposed on the videoEntry
              // object. An alternative would be to get an instance from the public video feed
              // and check that.
              
              List<YouTubeMediaRating> ratings = videoEntry.getMediaGroup().getYouTubeRatings();
              if (ratings.size() == 0) {
                stateValue = "OKAY";
              } else {
                StringBuffer restrictionBuffer = new StringBuffer("RESTRICTED IN: ");
                for (YouTubeMediaRating rating : ratings) {
                  restrictionBuffer.append(rating.getCountries());
                }
                stateValue = restrictionBuffer.toString();
              }
            } else {
              stateValue = state.getState().toString();
            }
            if (!stateValue.equals(videoSubmission.getYoutubeState())) {
              log.info(String.format("YouTube state differs: '%s' (local) vs. '%s' (YT).",
                      videoSubmission.getYoutubeState(), stateValue));
              videoSubmission.setYoutubeState(stateValue);
              videoSubmission.setUpdated(now);
            }
            
            String title = videoEntry.getTitle().getPlainText();
            if (!title.equals(videoSubmission.getVideoTitle())) {
              log.info(String.format("Title differs: '%s' (local) vs. '%s' (YT).",
                      videoSubmission.getVideoTitle(), title));
              videoSubmission.setVideoTitle(title);
              videoSubmission.setUpdated(now);
            }

            String description = videoEntry.getMediaGroup().getDescription().getPlainTextContent();
            if (!description.equals(videoSubmission.getVideoDescription())) {
              log.info(String.format("Description differs: '%s' (local) vs. '%s' (YT).",
                      videoSubmission.getVideoDescription(), description));
              videoSubmission.setVideoDescription(description);
              videoSubmission.setUpdated(now);
            }

            List<String> tags = videoEntry.getMediaGroup().getKeywords().getKeywords();
            String sortedTags = Util.sortedJoin(tags, ",");
            if (!sortedTags.equals(videoSubmission.getVideoTags())) {
              log.info(String.format("Tags differs: '%s' (local) vs. '%s' (YT).",
                      videoSubmission.getVideoTags(), sortedTags));
              videoSubmission.setVideoTags(sortedTags);
              videoSubmission.setUpdated(now);
            }
          } catch (NullPointerException e) {
            log.info(String.format("Couldn't get metadata for video id '%s'. It may not have been" +
                        " accepted by YouTube.", videoId));
          }
         
          // Unconditionally update view count info, but don't call setUpdated() since this is an
          // auto-update.
          YtStatistics stats = videoEntry.getStatistics();
          if (stats != null) {
            videoSubmission.setViewCount(stats.getViewCount());
          }

          log.info(String.format("Finished syncing video id '%s'", videoId));
        }

        // It's important to update lastSynced even for videos that didn't change.
        videoSubmission.setLastSynced(now);
        pm.makePersistent(videoSubmission);
      } else {
        log.info(String.format("Data is fresh; %.2f seconds since last sync.", delta / 1000.0));
      }
      
      resp.setContentType("text/javascript");
      resp.getWriter().println(Util.GSON.toJson(videoSubmission));
    } catch (IllegalArgumentException e) {
      log.log(Level.WARNING, "", e);
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
    } finally {
      pm.close();
    }
  }
}