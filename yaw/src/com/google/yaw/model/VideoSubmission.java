package com.google.yaw.model;

import com.google.gson.annotations.Expose;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.jdo.annotations.Extension;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.compass.annotations.Searchable;
import org.compass.annotations.SearchableId;
import org.compass.annotations.SearchableProperty;

@SuppressWarnings("serial")
@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
@Searchable
public class VideoSubmission implements Serializable {

  @PrimaryKey
  @Extension(vendorName = "datanucleus", key = "gae.encoded-pk", value = "true")
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  @Expose
  @SearchableId
  private String id;

  @Expose
  @Persistent
  private String videoId = null;

  // The AuthSub token used when uploading this video.
  @Expose
  @Persistent
  private String authSubToken = null;

  // The article on the news site that this submission belongs to.
  @Expose
  @Persistent
  private Long assignmentId = null;

  @Expose
  @Persistent
  private String videoTitle = null;

  @Expose
  @Persistent
  @SearchableProperty
  private String videoDescription = null;

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
  private String youtubeName = null;

  @Expose
  @Persistent
  private String articleUrl = null;

  @Expose
  @Persistent
  private String notifyEmail = null;
  
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
  private boolean isInPlaylist = false;

  @Expose
  @Persistent
  private Date updated;

  public enum ModerationStatus {
    UNREVIEWED, APPROVED, REJECTED, SPAM
  }
  
  @Expose
  @Persistent
  private int status;

  public enum VideoSource {
    NEW_UPLOAD, EXISTING_VIDEO
  }    

  @Expose
  @Persistent
  private int videoSource;  
  

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
      String description, String tagList, String uploader, String authSubToken, 
      VideoSource videoSource) {
    this.articleUrl = articleUrl;
    this.videoId = videoId;
    this.authSubToken = authSubToken;
    this.assignmentId = assignmentId;
    this.youtubeName = uploader;    
    this.videoTitle = title;
    this.videoDescription = description;
    this.videoTags = tagList;
    
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
    setStatus(ModerationStatus.UNREVIEWED);
  }  
  
  /**
   * Get the moderation status of the video.
   * 
   * @return The enumeration value representing this submission's status.
   */
  public ModerationStatus getStatus() {
    return ModerationStatus.values()[status];
  }

  /**
   * Set the moderation status of the video.
   * 
   * @param status
   *          The new status.
   */
  public void setStatus(ModerationStatus status) {
    this.status = status.ordinal();
  }

  public VideoSource getVideoSource() {
    return VideoSource.values()[videoSource];
  }
  
  public void setVideoSource(VideoSource videoSource) {
    this.videoSource = videoSource.ordinal();
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
   * Get the AuthSub token associated with this video upload. Unless the token
   * has been revoked or expired (after a year), you should be able to update
   * the related video using this as your credentials.
   * 
   * @return A YouTube video ID
   */
  public String getAuthSubToken() {
    return authSubToken;
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
    return youtubeName;
  }

  /**
   * Set the YouTube user who uploaded this video.
   * 
   * @param youTubeName
   *          A YouTube username.
   */
  public void setYouTubeName(String youTubeName) {
    this.youtubeName = youTubeName;
  }

  @SearchableProperty
  public String getVideoTitle() {
    return videoTitle;
  }

  public void setVideoTitle(String videoTitle) {
    this.videoTitle = videoTitle;
  }

  public String getVideoDescription() {
    return videoDescription;
  }

  public void setVideoDescription(String videoDescription) {
    this.videoDescription = videoDescription;
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

  public void setAuthSubToken(String authSubToken) {
    this.authSubToken = authSubToken;
  }

  public void setCreated(Date created) {
    this.created = created;
  }
  public void setUpdated(Date updated) {
    this.updated = updated;
  }

  public void setStatus(int status) {
    this.status = status;
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

  public String getVideoUrl() {
    return "http://www.youtube.com/v/" + videoId;
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
  
  public boolean getIsInPlaylist() {
    return isInPlaylist;
  }
  
  public void setIsInPlaylist(boolean isInPlaylist) {
    this.isInPlaylist = isInPlaylist;
  }
}
