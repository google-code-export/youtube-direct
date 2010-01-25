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

package com.google.ytd.youtube;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.apphosting.api.DeadlineExceededException;
import com.google.gdata.data.youtube.VideoEntry;
import com.google.gdata.data.youtube.YouTubeMediaRating;
import com.google.gdata.data.youtube.YtPublicationState;
import com.google.gdata.data.youtube.YtStatistics;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.ytd.dao.UserAuthTokenDao;
import com.google.ytd.model.VideoSubmission;
import com.google.ytd.util.Util;

/**
 * Servlet that syncs metadata from YouTube with the local datastore.
 * 
 * YouTube metadata (fetched via the API) is always considered authoritative.
 * This servlet is meant to be pinged periodically via App Engine's cron
 * functionality. Invoking it once every few minutes should be enough, though it
 * may need to be tweaked if there are a large number of VideoSubmission
 * entries. Don't invoke this more than once every 30 seconds because this may
 * take up to 30 seconds to complete.
 */
@Singleton
public class SyncMetadata extends HttpServlet {
  private static final Logger log = Logger.getLogger(SyncMetadata.class.getName());
  @Inject
  private Util util;
  @Inject
  private PersistenceManagerFactory pmf;
  @Inject
  private YouTubeApiHelper apiManager;
  @Inject
  private UserAuthTokenDao userAuthTokenDao;

  @SuppressWarnings("unchecked")
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    // The number of videos processed in this invocation.
    int count = 0;
    // The total number of videos in the datastore.
    int total = 0;

    PersistenceManager pm = pmf.getPersistenceManager();

    try {
      log.info("Starting sync operation...");

      // Get a list of all videos in the datastore, with the ones with the
      // oldest sync date listed
      // first. There is a timetout of 30 seconds per HTTP request in App
      // Engine, so it is possible
      // that not all these videos will be synced before a
      // DeadlineExceededException. This shouldn't
      // matter, as the ones that do get processed should have their lastSynced
      // updated, and
      // move to the end of the list the next time this is invoked.
      Query query = pm.newQuery(VideoSubmission.class);
      query.setOrdering("lastSynced asc");
      List<VideoSubmission> videoSubmissions = (List<VideoSubmission>) query.execute();
      total = videoSubmissions.size();

      for (VideoSubmission videoSubmission : videoSubmissions) {
        Date now = new Date();

        // Create a new instance each time through the loop, since changing
        // AuthSub tokens for an
        // existing instance doesn't seem to work.
        apiManager.setToken(userAuthTokenDao.getUserAuthToken(videoSubmission.getYouTubeName())
            .getAuthSubToken());

        String videoId = videoSubmission.getVideoId();
        log.info(String.format("Syncing video id '%s'", videoId));

        // This will retrieve video info from the Uploads feed of the user who
        // owns the video.
        // This should always be the freshest data, but it relies on the AuthSub
        // token being valid.
        VideoEntry videoEntry = apiManager.getUploadsVideoEntry(videoId);
        if (videoEntry == null) {
          // Try an unauthenticated request to the specific user's uploads feed
          // next.
          videoEntry = apiManager.getUploadsVideoEntry(videoSubmission.getYouTubeName(), videoId);

          if (videoEntry == null) {
            // Fall back on looking for the video in the public feed.
            videoEntry = apiManager.getVideoEntry(videoId);

            if (videoEntry == null) {
              // The video must have been deleted...
              log.info(String.format("Unable to find YouTube video id '%s'.", videoId));
              videoSubmission.setYouTubeState("NOT_FOUND");
            }
          }
        }

        if (videoEntry != null) {
          try {
            YtPublicationState state = videoEntry.getPublicationState();
            String stateValue;
            if (state == null) {
              // TODO: Find some way to check whether the video is embeddable
              // and/or private, and
              // populate that info. Because we're getting the video from the
              // authenticated
              // uploads feed (by default), that info isn't easily exposed on
              // the videoEntry
              // object. An alternative would be to get an instance from the
              // public video feed
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
            if (!stateValue.equals(videoSubmission.getYouTubeState())) {
              log.info(String.format("YouTube state differs: '%s' (local) vs. '%s' (YT).",
                  videoSubmission.getYouTubeState(), stateValue));
              videoSubmission.setYouTubeState(stateValue);
              videoSubmission.setUpdated(now);
            }

            String title = videoEntry.getTitle().getPlainText();
            if (!title.equals(videoSubmission.getVideoTitle())) {
              log.info(String.format("Title differs: '%s' (local) vs. '%s' (YT).", videoSubmission
                  .getVideoTitle(), title));
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
            String sortedTags = util.sortedJoin(tags, ",");
            if (!sortedTags.equals(videoSubmission.getVideoTags())) {
              log.info(String.format("Tags differs: '%s' (local) vs. '%s' (YT).", videoSubmission
                  .getVideoTags(), sortedTags));
              videoSubmission.setVideoTags(sortedTags);
              videoSubmission.setUpdated(now);
            }
          } catch (NullPointerException e) {
            log.info(String.format("Couldn't get metadata for video id '%s'. It may not have been"
                + " accepted by YouTube.", videoId));
          }

          // Unconditionally update view count info, but don't call setUpdated()
          // since this is an
          // auto-update.
          YtStatistics stats = videoEntry.getStatistics();
          if (stats != null) {
            videoSubmission.setViewCount(stats.getViewCount());
          }

          log.info(String.format("Finished syncing video id '%s'", videoId));
        }

        // It's important to update lastSynced even for videos that didn't
        // change; otherwise, if
        // syncing takes longer than the timeout, the same unchanged videos
        // might be examined again
        // and again, with newer videos never getting checked.
        videoSubmission.setLastSynced(now);
        pm.makePersistent(videoSubmission);

        count++;
      }
    } catch (DeadlineExceededException e) {
      // App Engine imposes a 30 second timeout on requests (the development
      // server doesn't).
      // We should be able to handle this gracefully by logging it and closing
      // the PM connection.
      log.info("Deadline exceeded; aborting sync.");
    } finally {
      pm.close();

      String message = String.format("Ending sync. %d of %d videos checked.", count, total);
      log.info(message);
      resp.setContentType("text/plain");
      resp.getWriter().println(message);
    }
  }
}
