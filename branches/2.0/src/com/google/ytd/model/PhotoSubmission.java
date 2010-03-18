package com.google.ytd.model;

import java.util.Date;

import javax.jdo.annotations.Extension;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.gson.annotations.Expose;

/*
 * Represents the meta grouping of a set of photo entries.
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class PhotoSubmission {

  @PrimaryKey
  @Extension(vendorName = "datanucleus", key = "gae.encoded-pk", value = "true")
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  @Expose
  private String id = null;

  @Persistent
  @Expose
  private String notifyEmail = null;

  @Persistent
  @Expose
  private Long assignmentId = null;

  @Persistent
  @Expose
  Date created = null;

  @Persistent
  @Expose
  Date updated = null;  
  
  @Persistent
  @Expose
  private String articleUrl = null;

  @Persistent
  @Expose
  private String title = null;

  @Persistent
  @Expose
  private String description = null;

  @Persistent
  @Expose
  private String location = null;

  @Persistent
  @Expose
  private int numberOfPhotos = 0;

  @Persistent
  @Expose
  private String adminNotes = null;

  public PhotoSubmission(Long assignmentId, String articleUrl, String email, String title,
      String description, String location, int numberOfPhotos) {
    this.assignmentId = assignmentId;
    this.articleUrl = articleUrl;
    this.notifyEmail = email;
    this.title = title;
    this.description = description;
    this.location = location;
    this.numberOfPhotos = numberOfPhotos;
    this.created = new Date();
    this.updated = this.created;
  }

  public String getId() {
    return id;
  }

  public String getNotifyEmail() {
    return notifyEmail;
  }

  public Long getAssignmentId() {
    return assignmentId;
  }

  public Date getCreated() {
    return created;
  }

  public Date getUpdated() {
    return updated;
  }
  
  
  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public String getLocation() {
    return location;
  }

  public void setNumberOfPhotos(int numberOfPhotos) {
    this.numberOfPhotos = numberOfPhotos;
  }

  public int getNumberOfPhotos() {
    return numberOfPhotos;
  }

  public void setArticleUrl(String articleUrl) {
    this.articleUrl = articleUrl;
  }

  public String getArticleUrl() {
    return articleUrl;
  }

  public void setAdminNotes(String adminNotes) {
    this.adminNotes = adminNotes;
  }

  public String getAdminNotes() {
    return adminNotes;
  }
  
  public void setUpdated(Date date) {
    this.updated = date;
  }
}