package com.google.yaw.model;

import com.google.appengine.api.datastore.Key;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Model class for a video submission to the system.
 * 
 */
@SuppressWarnings("serial")
@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
public class VideoSubmission implements Serializable {

	// The default "version" of this model
	private static int DEFAULT_SCHEMA_VERSION = 1;

	// The version of the model - used for upgrading entities if the data model
	// changes.
	@Persistent
	private int SCHEMA_VERSION;
	
	@PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Key key;

	// The id of the submission is based on the YouTube video ID.
	// A submission is not allowed to be for multiple "articles".
	@Persistent
	private String id = null;

	@Persistent
	private String videoId = null;

	// The AuthSub token used when uploading this video.
	@Persistent
	private String authSubToken = null;

	// The article on the news site that this submission belongs to.
	@Persistent
	private String assignmentId = null;

	@Persistent
	private String videoTitle = null;

	@Persistent
	private String videoDescription = null;

	@Persistent
	private String videoLocation = null;

	@Persistent
	private String videoTagList = null;

	@Persistent
	private Date created;

	// A string index used for pagination in app engine
	@Persistent
	private String createdIndex;

	@Persistent
	private Date updated;

	public enum ModerationStatus {
		UNREVIEWED, APPROVED, REJECTED
	}

	@Persistent
	private int status;

	// YouTube username of the uploader
	@Persistent
	private String youTubeName = null;

	@Persistent
	private String articleUrl = null;
	
	@Persistent
	private Long assignmentKey = null;

	/**
	 * Create a new video submission entry object for the datastore.
	 * 
	 * @param videoId
	 *            The YouTube video ID of the upload
	 * @param assignmentId
	 *            The news site article ID
	 * @param uploader
	 *            The YouTube username of the uploader
	 */
	public VideoSubmission(String assignmentId,
			String articleUrl, String videoId, String title,
			String description, String tagList, String uploader,
			String authSubToken, Long assignmentKey) {
		this.SCHEMA_VERSION = DEFAULT_SCHEMA_VERSION;
		this.articleUrl = articleUrl;
		this.id = "video-" + videoId;
		this.videoId = videoId;
		this.authSubToken = authSubToken;
		this.assignmentId = assignmentId;
		this.youTubeName = uploader;
		this.created = new Date();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		df.setTimeZone(TimeZone.getTimeZone("GMT"));
		this.createdIndex = df.format(this.created) + "|" + videoId;
		this.updated = new Date();
		setStatus(ModerationStatus.UNREVIEWED);
		this.videoTitle = title;
		this.videoDescription = description;
		this.videoTagList = tagList;
		this.assignmentKey = assignmentKey;
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
	 *            The new status.
	 */
	public void setStatus(ModerationStatus status) {
		this.status = status.ordinal();
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
	 * @return The site-specific article ID to tie this submission to.
	 */
	public String getAssignmentId() {
		return assignmentId;
	}

	/**
	 * Sets the site-specific article ID the submission is tied to.
	 * 
	 * @param assignmentId
	 *            The new ID.
	 */
	public void setAssignmentId(String assignmentId) {
		this.assignmentId = assignmentId;
	}

	/**
	 * Update the schema version when the model changes.
	 * 
	 * @param version
	 *            The new version.
	 */
	public void setSchemaVersion(int version) {
		this.SCHEMA_VERSION = version;
	}

	/**
	 * 
	 * @return The current schema version of this entity
	 */
	public int getSchemaVersion() {
		return SCHEMA_VERSION;
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
	 * @return The index value based upon the creation date for this entity.
	 */
	public String getCreatedIndex() {
		return this.createdIndex;
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
	 * @param uploader
	 *            A YouTube username.
	 */
	public void setYouTubeName(String youTubeName) {
		this.youTubeName = youTubeName;
	}

	public static int getDEFAULT_SCHEMA_VERSION() {
		return DEFAULT_SCHEMA_VERSION;
	}

	public static void setDEFAULT_SCHEMA_VERSION(int default_schema_version) {
		DEFAULT_SCHEMA_VERSION = default_schema_version;
	}

	public int getSCHEMA_VERSION() {
		return SCHEMA_VERSION;
	}

	public void setSCHEMA_VERSION(int schema_version) {
		SCHEMA_VERSION = schema_version;
	}

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

	public String getVideoTagList() {
		return videoTagList;
	}

	public void setVideoTagList(String videoTagList) {
		this.videoTagList = videoTagList;
	}

	public void setId(String id) {
		this.id = id;
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

	public void setCreatedIndex(String createdIndex) {
		this.createdIndex = createdIndex;
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

	public Key getKey() {
	    return key;
	}
	
	public Long getAssignmentKey() {
	    return assignmentKey;
	}
}
