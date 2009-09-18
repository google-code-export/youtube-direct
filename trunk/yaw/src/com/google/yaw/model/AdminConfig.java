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
  private String youTubeUsername;
  
  @Expose
  @Persistent
  private String youTubePassword;
  
  @Expose
  @Persistent
  private String defaultTag;

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
  
  @Expose
  @Persistent
  private String loginInstruction;
  
  public AdminConfig() {
    // Fetch default values from appengine-web.xml system props
    clientId = System.getProperty("com.google.yaw.YTClientID");
    developerKey = System.getProperty("com.google.yaw.YTDeveloperKey");
    youTubeUsername = System.getProperty("com.google.yaw.YTUsername");
    youTubePassword = System.getProperty("com.google.yaw.YTPassword");    
    defaultTag = System.getProperty("com.google.yaw.BrandingTag");
    
    moderationMode = ModerationModeType.MOD_REQUIRED.ordinal();
    brandingMode = BrandingModeType.ON.ordinal();
    submissionMode = SubmissionModeType.NEW_OR_EXISTING.ordinal();
    
    loginInstruction = 
      "This%20website%20is%20using%20YouTube%20AnyWhere%20to%20receive%20video%20response%20" + 
      "submission.%3Cbr%3E%20%20%0A%3Cbr%3E%0APlease%20login%20to%20your%20YouTube%20account%20" +
      "to%20proceed.%20%3Cbr%3E%0A%3Cbr%3E%0AYouTube%20API%20ToS%20%5B%20%3Ca%20target%3D%" + 
      "22_blank%22%20href%3D%22http%3A//code.google.com/apis/youtube/terms.html%22%3E" +
      "read%3C/a%3E%20%5D%20%20";
    
    setUpdated(new Date());
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
  
  public String getYouTubeUsername() {
    return youTubeUsername;
  }
  
  public void setYouTubeUsername(String youTubeUsername) {
    this.youTubeUsername = youTubeUsername;
  }
  
  public String getYouTubePassword() {
    return youTubePassword;
  }
  
  public void setYouTubePassword(String youTubePassword) {
    this.youTubePassword = youTubePassword;
  }
  
  public String getDefaultTag() {
    return defaultTag;
  }
  
  public void setDefaultTag(String defaultTag) {
    this.defaultTag = defaultTag;
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

  public void setLoginInstruction(String loginInstruction) {
    this.loginInstruction = loginInstruction;
  }

  public String getLoginInstruction() {
    return loginInstruction;
  }

  public void setUpdated(Date updated) {
    this.updated = updated;
  }

  public Date getUpdated() {
    return updated;
  }

}
