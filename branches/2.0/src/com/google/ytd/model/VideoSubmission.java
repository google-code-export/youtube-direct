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

package com.google.ytd.model;

import java.io.Serializable;
import java.util.Date;

import javax.jdo.annotations.Extension;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Text;
import com.google.gson.annotations.Expose;

/**
 * Model class for video submissions.
 */
@SuppressWarnings("serial")
@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
public class VideoSubmission implements Serializable {
  private static final String YOUTUBE_VIDEO_URL_FORMAT = "http://www.youtube.com/v/%s";
  private static final String YOUTUBE_WATCH_URL_FORMAT = "http://www.youtube.com/watch#v=%s";

  @PrimaryKey
  @Extension(vendorName = "datanucleus", key = "gae.encoded-pk", value = "true")
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  @Expose
  private String id;

  @Expose
  @Persistent
  private String videoId = null;

  // The article on the news site that this submission belongs to.
  @Expose
  @Persistent
  private Long assignmentId = null;

  @Expose
  @Persistent
  private String videoTitle = null;

  @Expose
  @Persistent
  private Text videoDescription = null;

  @Expose
  @Persistent
  private String videoLocation = null;

  @Expose
  @Persistent
  private String videoDate = null;

  @Expose
  @Persistent
  private String videoTags = null;

  @Expose
  @Persistent
  private String youTubeName = null;

  @Expose
  @Persistent
  private String articleUrl = null;

  @Expose
  @Persistent
  private String notifyEmail = null;

  @Expose
  @Persistent
  private String phoneNumber = null;  
  
  @Expose
  @Persistent
  private String adminNotes = null;

  @Expose
  @Persistent
  private Date created;

  @Persistent
  private Date lastSynced;

  @Expose
  @Persistent
  private long viewCount;

  @Expose
  @Persistent
  private String youTubeState = null;

  @Expose
  @Persistent
  private boolean isInPlaylist = false;

  @Expose
  @Persistent
  private Date updated;

  public enum ModerationStatus {
    UNREVIEWED,
    APPROVED,
    REJECTED,
    SPAM
  }

  @Expose
  @Persistent
  private ModerationStatus status;

  public enum VideoSource {
    NEW_UPLOAD,
    EXISTING_VIDEO,
    MOBILE_SUBMIT
  }

  @Expose
  @Persistent
  private VideoSource videoSource;

  /**
   * Create a new video submission entry object for the datastore.
   * 
   * @param videoId
   *          The YouTube video ID of the upload
   * @param assignmentId
   *          The news site article ID
   * @param uploader
   *          The YouTube username of the uploader
   */
  public VideoSubmission(Long assignmentId, String articleUrl, String videoId, String title,
      String description, String tagList, String uploader, VideoSource videoSource) {
    this.articleUrl = articleUrl;
    this.videoId = videoId;
    this.assignmentId = assignmentId;
    this.youTubeName = uploader;
    this.videoTitle = title;
    this.videoDescription = new Text(description);
    this.videoTags = tagList;
    this.youTubeState = "UNKNOWN";
    this.created = new Date();
    this.updated = this.created;
    this.lastSynced = this.created;
    this.viewCount = -1;
    setStatus(ModerationStatus.UNREVIEWED);
    setVideoSource(videoSource);
  }

  public VideoSubmission(Long assignmentId) {
    this.assignmentId = assignmentId;
    this.created = new Date();
    this.updated = this.created;
    this.lastSynced = this.created;
    this.viewCount = -1;
    this.youTubeState = "UNKNOWN";
    setStatus(ModerationStatus.UNREVIEWED);
  }

  public VideoSubmission() {
    this.created = new Date();
    this.updated = this.created;
    this.lastSynced = this.created;
    this.viewCount = -1;
    this.youTubeState = "UNKNOWN";
    setStatus(ModerationStatus.UNREVIEWED);
  }

  /**
   * Get the moderation status of the video.
   * 
   * @return The enumeration value representing this submission's status.
   */
  public ModerationStatus getStatus() {
    return this.status;
  }

  /**
   * Set the moderation status of the video.
   * 
   * @param status
   *          The new status.
   */
  public void setStatus(ModerationStatus status) {
    this.status = status;
  }

  public VideoSource getVideoSource() {
    return this.videoSource;
  }

  public void setVideoSource(VideoSource videoSource) {
    this.videoSource = videoSource;
  }

  /**
   * Get the YouTube video ID of this submission
   * 
   * @return A YouTube video ID
   */
  public String getVideoId() {
    return videoId;
  }

  /**
   * @return The date and time this submission was created.
   */
  public Date getCreated() {
    return created;
  }

  /**
   * @return The last date and time this submission was modified.
   */
  public Date getUpdated() {
    return updated;
  }

  /**
   * Sets the site-specific article ID the submission is tied to.
   * 
   * @param assignmentId
   *          The new ID.
   */
  public void setAssignmentId(Long assignmentId) {
    this.assignmentId = assignmentId;
  }

  /**
   * 
   * @return The ID of this entity
   */
  public String getId() {
    return this.id;
  }

  /**
   * 
   * @return The YouTube user who submitted this video.
   */
  public String getYouTubeName() {
    return youTubeName;
  }

  /**
   * Set the YouTube user who uploaded this video.
   * 
   * @param youTubeName
   *          A YouTube username.
   */
  public void setYouTubeName(String youTubeName) {
    this.youTubeName = youTubeName;
  }

  public String getVideoTitle() {
    return videoTitle;
  }

  public void setVideoTitle(String videoTitle) {
    this.videoTitle = videoTitle;
  }

  public String getVideoDescription() {
    return videoDescription.getValue();
  }

  public void setVideoDescription(String videoDescription) {
    this.videoDescription = new Text(videoDescription);
  }

  public String getVideoLocation() {
    return videoLocation;
  }

  public void setVideoLocation(String videoLocation) {
    this.videoLocation = videoLocation;
  }

  public String getVideoTags() {
    return videoTags;
  }

  public void setVideoTags(String videoTags) {
    this.videoTags = videoTags;
  }

  public void setVideoId(String videoId) {
    this.videoId = videoId;
  }

  public void setCreated(Date created) {
    this.created = created;
  }

  public void setUpdated(Date updated) {
    this.updated = updated;
  }

  public String getArticleUrl() {
    return articleUrl;
  }

  public void setArticleUrl(String articleUrl) {
    this.articleUrl = articleUrl;
  }

  public Long getAssignmentId() {
    return assignmentId;
  }

  public void setNotifyEmail(String email) {
    this.notifyEmail = email;
  }

  public String getNotifyEmail() {
    return notifyEmail;
  }

  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }  
  
  public String getPhoneNumber() {
    return phoneNumber;
  }  
  
  public String getVideoUrl() {
    return String.format(YOUTUBE_VIDEO_URL_FORMAT, videoId);
  }

  public String getWatchUrl() {
    return String.format(YOUTUBE_WATCH_URL_FORMAT, videoId);
  }

  public Date getLastSynced() {
    return lastSynced;
  }

  public void setLastSynced(Date lastSynced) {
    this.lastSynced = lastSynced;
  }

  public void setVideoDate(String videoDate) {
    this.videoDate = videoDate;
  }

  public String getVideoDate() {
    return videoDate;
  }

  public long getViewCount() {
    return viewCount;
  }

  public void setViewCount(long viewCount) {
    this.viewCount = viewCount;
  }

  public boolean isInPlaylist() {
    return isInPlaylist;
  }

  public void setIsInPlaylist(boolean isInPlaylist) {
    this.isInPlaylist = isInPlaylist;
  }

  public void setAdminNotes(String adminNotes) {
    this.adminNotes = adminNotes;
  }

  public String getAdminNotes() {
    return adminNotes;
  }

  public String getYouTubeState() {
    return youTubeState;
  }

  public void setYouTubeState(String youtubeState) {
    this.youTubeState = youtubeState;
  }
}
