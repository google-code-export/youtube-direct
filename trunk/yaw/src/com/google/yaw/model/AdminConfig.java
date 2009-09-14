package com.google.yaw.model;

import java.io.Serializable;
import java.util.Date;

import javax.jdo.annotations.Extension;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.gson.annotations.Expose;

/**
 * Model class for all web application settings.
 */
@PersistenceCapable(detachable = "true")
public class AdminConfig implements Serializable {
  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Long id;
  
  @Expose
  @Persistent
  private String developerKey;
  
  @Expose
  @Persistent
  private String clientId;

  @Expose
  @Persistent
  private Date updated;

  public enum ModerationModeType {
    MOD_REQUIRED, NO_MOD
  }
  
  @Expose
  @Persistent
  private int moderationMode = ModerationModeType.MOD_REQUIRED.ordinal();  

  public enum BrandingModeType {
    ON, OFF
  }
  
  @Expose
  @Persistent
  private int brandingMode = BrandingModeType.ON.ordinal();    
  
  public enum SubmissionModeType {
    NEW_OR_EXISTING, NEW_ONLY
  }
  
  @Expose
  @Persistent
  private int submissionMode = SubmissionModeType.NEW_OR_EXISTING.ordinal();     
  
  public AdminConfig() {
    // fetch default clientId and developerKey from appengine-web.xml system props
    clientId = System.getProperty("com.google.yaw.YTClientID");
    developerKey = System.getProperty("com.google.yaw.YTDeveloperKey");
    
    moderationMode = ModerationModeType.MOD_REQUIRED.ordinal();
    brandingMode = BrandingModeType.ON.ordinal();
    submissionMode = SubmissionModeType.NEW_OR_EXISTING.ordinal();
  }
  
  public Long getId() {
    return id;
  }

  public String getDeveloperKey() {
    return developerKey;
  }

  public void setDeveloperKey(String developerKey) {
    this.developerKey = developerKey;
  }

  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public void setModerationMode(int moderationMode) {
    this.moderationMode = moderationMode;
  }

  public int getModerationMode() {
    return moderationMode;
  }

  public void setBrandingMode(int brandingMode) {
    this.brandingMode = brandingMode;
  }

  public int getBrandingMode() {
    return brandingMode;
  }

  public void setSubmissionMode(int submissionMode) {
    this.submissionMode = submissionMode;
  }

  public int getSubmissionMode() {
    return submissionMode;
  }

}
