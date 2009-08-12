package com.google.yaw.model;

import javax.jdo.annotations.Extension;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
public class UserSession {

	@PrimaryKey
	@Extension(vendorName = "datanucleus", key = "gae.encoded-pk", value = "true")
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private String id = null;

	@Persistent
	private String youTubeName = null;

	@Persistent
	private String authSubToken = null;

	@Persistent
	private String assignmentId = null;

	@Persistent
	private String articleUrl = null;

	@Persistent
	private String selfUrl = null;

	@Persistent
	private String videoTitle = null;
	
	@Persistent
	private String videoDescription = null;
	
	@Persistent
	private String videoLocation = null;
	
	@Persistent
	private String videoTagList = null;	
	
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
	
	public String getSelfUrl() {
		return selfUrl;
	}

	public void setSelfUrl(String selfUrl) {
		this.selfUrl = selfUrl;
	}

	public UserSession() {

	}

	public String getassignmentId() {
		return assignmentId;
	}

	public void setassignmentId(String assignmentId) {
		this.assignmentId = assignmentId;
	}

	public String getArticleUrl() {
		return articleUrl;
	}

	public void setArticleUrl(String articleUrl) {
		this.articleUrl = articleUrl;
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

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public String getYouTubeName() {
		return youTubeName;
	}

	public String getAuthSubToken() {
		return authSubToken;
	}

	public void setYouTubeName(String name) {
		youTubeName = name;
	}

	public void setAuthSubToken(String token) {
		authSubToken = token;
	}
}
