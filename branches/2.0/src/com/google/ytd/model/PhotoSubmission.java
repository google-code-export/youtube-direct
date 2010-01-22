package com.google.ytd.model;

import java.util.Date;

import javax.jdo.annotations.Extension;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Blob;

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
  private Blob content = null;

  @Persistent
  private int size = 0;

  @Persistent
  String contentType = null;

  @Persistent
  Date created = null;

  public PhotoSubmission(Long assignmentId, String email, byte[] content, String contentType) {
    this.assignmentId = assignmentId;
    this.content = new Blob(content);
    this.contentType = contentType;
    this.size = content.length;
    this.created = new Date();
    this.email = email;
  }

  public String getId() {
    return id;
  }

  public String getEmail() {
    return email;
  }

  public Blob getContent() {
    return content;
  }

  public int getSize() {
    return size;
  }

  public String getContentType() {
    return contentType;
  }

  public Date getCreated() {
    return created;
  }

  public Long getAssignmentId() {
    return assignmentId;
  }
}