package com.google.ytd.model;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.Blob;
import com.google.gson.annotations.Expose;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class PhotoEntry {

  @PrimaryKey
  @Expose
  // The blob key string is the unique id of PhotoEntry
  private String id = null;

  @Persistent
  @Expose
  // ID that points to the PhotoSubmission that contains the meta data of the submission session
  private String submissionId = null;

  @Persistent
  private BlobKey blobKey = null;

  @Persistent
  @Expose
  private String imageUrl = null;

  @Persistent
  @Expose
  private String thumbnailUrl = null;

  @Persistent
  private Blob thumbnail = null;

  @Persistent
  @Expose
  private String format = null;

  public enum ModerationStatus {
    UNREVIEWED,
    APPROVED,
    REJECTED
  }

  @Expose
  @Persistent
  private ModerationStatus status;

  public PhotoEntry(String submissionId, BlobKey blobKey, String format, Blob thumbnail) {
    this.blobKey = blobKey;
    this.id = blobKey.getKeyString();
    this.submissionId = submissionId;
    this.imageUrl = "/image?id=" + this.id;
    this.thumbnailUrl = "/thumb?id=" + this.id;
    this.status = ModerationStatus.UNREVIEWED;
    this.format = format;
    this.thumbnail = thumbnail;
  }

  public String getId() {
    return id;
  }

  public String getSubmissionId() {
    return submissionId;
  }

  public BlobKey getBlobKey() {
    return blobKey;
  }

  public void setStatus(ModerationStatus status) {
    this.status = status;
  }

  public ModerationStatus getStatus() {
    return status;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public String getThumbnailUrl() {
    return thumbnailUrl;
  }

  public String getFormat() {
    return format;
  }

  public Blob getThumbnail() {
    return thumbnail;
  }
}