package com.google.ytd.model;

import java.util.Date;

import javax.jdo.annotations.Extension;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.blobstore.BlobKey;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class PhotoSubmission {

  @PrimaryKey
  @Extension(vendorName = "datanucleus", key = "gae.encoded-pk", value = "true")
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private String id = null;

  @Persistent
  private String email = null;

  @Persistent
  private Long assignmentId = null;

  @Persistent
  private BlobKey blobKey = null;

  @Persistent
  Date created = null;
  
  @Persistent
  private String title = null;
  
  @Persistent
  private String description = null;
  
  @Persistent
  private String location = null;
  
  @Persistent
  private String batchId = null;

  public PhotoSubmission(Long assignmentId, BlobKey blobKey, String batchId, String email,
      String title, String description, String location) {
    this.assignmentId = assignmentId;
    this.blobKey = blobKey;
    this.batchId = batchId;
    this.email = email;
    this.title = title;
    this.description = description;
    this.location = location;
    
    this.created = new Date();
  }

  public String getId() {
    return id;
  }

  public String getEmail() {
    return email;
  }

  public Long getAssignmentId() {
    return assignmentId;
  }

  public BlobKey getBlobKey() {
    return blobKey;
  }
  
  public String getBatchId() {
    return batchId;
  }

  public Date getCreated() {
    return created;
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
}