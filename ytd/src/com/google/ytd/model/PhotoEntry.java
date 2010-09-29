package com.google.ytd.model;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.gson.annotations.Expose;

import java.util.Date;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class PhotoEntry {
  @PrimaryKey
  @Expose // The blob key string is the unique id of PhotoEntry
  private String id = null;

  @Persistent
  @Expose // ID that points to the PhotoSubmission that contains the meta data of
         // the submission session
  private String submissionId = null;

  @Persistent
  private BlobKey blobKey = null;

  @Persistent
  @Expose
  private String picasaUrl = null;

  @Persistent
  @Expose
  private String format = null;

  @Persistent
  @Expose
  private String thumbnailUrl = null;

  @Persistent
  @Expose
  private String imageUrl = null;

  @Persistent
  @Expose
  private Long originalFileSize = null;

  @Persistent
  @Expose
  private String originalFileName = null;

  @Persistent
  @Expose
  private String resumableUploadUrl = null;

  @SuppressWarnings("unused")
  @Expose
  @Persistent
  private Date created;

  public enum ModerationStatus {
    UNREVIEWED, APPROVED, REJECTED
  }

  @Expose
  @Persistent
  private ModerationStatus status;

  public PhotoEntry(String submissionId, BlobKey blobKey, String format) {
    this.blobKey = blobKey;
    this.id = blobKey.getKeyString();
    this.submissionId = submissionId;
    this.status = ModerationStatus.UNREVIEWED;
    this.format = format;

    this.created = new Date();
  }
  
  public PhotoEntry(String submissionId, String id, String format) {
    this.submissionId = submissionId;
    this.id = id;
    this.status = ModerationStatus.UNREVIEWED;
    this.format = format;

    this.created = new Date();
  }

  public String getId() {
    return id;
  }

  public String getSubmissionId() {
    return submissionId;
  }

  public void setBlobKey(BlobKey blobKey) {
    this.blobKey = blobKey;
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

  public String getFormat() {
    return format;
  }

  public String getPicasaUrl() {
    return picasaUrl;
  }

  public void setPicasaUrl(String picasaUrl) {
    this.picasaUrl = picasaUrl;
  }

  public String getThumbnailUrl() {
    return thumbnailUrl;
  }

  public void setThumbnailUrl(String thumbnailUrl) {
    this.thumbnailUrl = thumbnailUrl;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public void setImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
  }

  public long getOriginalFileSize() {
    if (originalFileSize == null) {
      originalFileSize = new Long(0);
    }
    
    return originalFileSize.longValue();
  }

  public void setOriginalFileSize(long originalFileSize) {
    this.originalFileSize = originalFileSize;
  }

  public String getOriginalFileName() {
    return originalFileName;
  }

  public void setOriginalFileName(String originalFileName) {
    this.originalFileName = originalFileName;
  }

  /**
   * @return the resumableUploadUrl
   */
  public String getResumableUploadUrl() {
    return resumableUploadUrl;
  }

  /**
   * @param resumableUploadUrl the resumableUploadUrl to set
   */
  public void setResumableUploadUrl(String resumableUploadUrl) {
    this.resumableUploadUrl = resumableUploadUrl;
  }
}
