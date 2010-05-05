package com.google.ytd.command;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gdata.data.youtube.VideoEntry;
import com.google.gdata.data.youtube.YouTubeMediaRating;
import com.google.gdata.data.youtube.YtPublicationState;
import com.google.gdata.data.youtube.YtStatistics;
import com.google.inject.Inject;
import com.google.ytd.dao.UserAuthTokenDao;
import com.google.ytd.dao.VideoSubmissionDao;
import com.google.ytd.model.UserAuthToken;
import com.google.ytd.model.VideoSubmission;
import com.google.ytd.util.Util;
import com.google.ytd.youtube.YouTubeApiHelper;

public class GetVideoDetails extends Command {
  private static final Logger LOG = Logger.getLogger(GetVideoSubmissions.class.getName());
  private static final long REFRESH_INTERVAL = 30 * 1000; // 30 seconds, in milliseconds

  private VideoSubmissionDao submissionDao = null;
  private YouTubeApiHelper apiManager = null;
  private UserAuthTokenDao userAuthTokenDao = null;

  @Inject
  private Util util;

  @Inject
  public GetVideoDetails(VideoSubmissionDao submissionDao, YouTubeApiHelper apiManager,
      UserAuthTokenDao userAuthTokenDao) {
    this.submissionDao = submissionDao;
    this.apiManager = apiManager;
    this.userAuthTokenDao = userAuthTokenDao;
  }

  @Override
  public JSONObject execute() throws JSONException {
    String submissionId = getParam("submissionId");

    if (util.isNullOrEmpty(submissionId)) {
      throw new IllegalArgumentException("Missing required param: videoId");
    }

    LOG.info(String.format("Sync'ing metadata for submission id %s", submissionId));

    VideoSubmission videoSubmission = submissionDao.getSubmissionById(submissionId);
    if (videoSubmission == null) {
      throw new IllegalArgumentException(String.format("Couldn't retrieve VideoSubmission with"
          + " id '%s' from the datastore.", submissionId));
    }

    Date now = new Date();
    long delta = now.getTime() - videoSubmission.getLastSynced().getTime();
    if (delta > REFRESH_INTERVAL) {
      UserAuthToken userAuthToken = 
        userAuthTokenDao.getUserAuthToken(videoSubmission.getYouTubeName());
    
      if (!userAuthToken.getAuthSubToken().isEmpty()) {
        apiManager.setAuthSubToken(userAuthToken.getAuthSubToken());
      } else {
        apiManager.setClientLoginToken(userAuthToken.getClientLoginToken());
      }

      String videoId = videoSubmission.getVideoId();

      // This will retrieve video info from the Uploads feed of the user who owns the video.
      // This should always be the freshest data, but it relies on the AuthSub token being valid.
      VideoEntry videoEntry = apiManager.getUploadsVideoEntry(videoId);
      if (videoEntry == null) {
        // Try an unauthenticated request to the specific user's uploads feed next.
        apiManager.setAuthSubToken("");
        videoEntry = apiManager.getUploadsVideoEntry(videoSubmission.getYouTubeName(), videoId);

        if (videoEntry == null) {
          // Fall back on looking for the video in the public feed.
          videoEntry = apiManager.getVideoEntry(videoId);

          if (videoEntry == null) {
            // The video must have been deleted...
            LOG.info(String.format("Unable to find YouTube video id '%s'.", videoId));
            videoSubmission.setYouTubeState("NOT_FOUND");
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
          if (!stateValue.equals(videoSubmission.getYouTubeState())) {
            LOG.info(String.format("YouTube state differs: '%s' (local) vs. '%s' (YT).",
                videoSubmission.getYouTubeState(), stateValue));
            videoSubmission.setYouTubeState(stateValue);
            videoSubmission.setUpdated(now);
          }

          String title = videoEntry.getTitle().getPlainText();
          if (!title.equals(videoSubmission.getVideoTitle())) {
            LOG.info(String.format("Title differs: '%s' (local) vs. '%s' (YT).", videoSubmission
                .getVideoTitle(), title));
            videoSubmission.setVideoTitle(title);
            videoSubmission.setUpdated(now);
          }

          String description = videoEntry.getMediaGroup().getDescription().getPlainTextContent();
          if (!description.equals(videoSubmission.getVideoDescription())) {
            LOG.info(String.format("Description differs: '%s' (local) vs. '%s' (YT).",
                videoSubmission.getVideoDescription(), description));
            videoSubmission.setVideoDescription(description);
            videoSubmission.setUpdated(now);
          }

          List<String> tags = videoEntry.getMediaGroup().getKeywords().getKeywords();
          String sortedTags = util.sortedJoin(tags, ",");
          if (!sortedTags.equals(videoSubmission.getVideoTags())) {
            LOG.info(String.format("Tags differs: '%s' (local) vs. '%s' (YT).", videoSubmission
                .getVideoTags(), sortedTags));
            videoSubmission.setVideoTags(sortedTags);
            videoSubmission.setUpdated(now);
          }
        } catch (NullPointerException e) {
          LOG.info(String.format("Couldn't get metadata for video id '%s'. It may not have been"
              + " accepted by YouTube.", videoId));
        }

        // Unconditionally update view count info, but don't call setUpdated() since this is an
        // auto-update.
        YtStatistics stats = videoEntry.getStatistics();
        if (stats != null) {
          videoSubmission.setViewCount(stats.getViewCount());
        }

        LOG.info(String.format("Finished syncing video id '%s'", videoId));
      }

      // It's important to update lastSynced even for videos that didn't change.
      videoSubmission.setLastSynced(now);
      submissionDao.save(videoSubmission);
    } else {
      LOG.info(String.format("Data is fresh; %.2f seconds since last sync.", delta / 1000.0));
    }

    JSONObject json = new JSONObject();
    json.put("videoSubmission", util.toJson(videoSubmission));
    return json;
  }
}
