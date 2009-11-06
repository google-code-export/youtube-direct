/* Copyright (c) 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.ytd.model;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
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
  private String youTubeAuthSubToken;
  
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
  
  @Expose
  @Persistent
  private String postSubmitMessage;

  @Expose
  @Persistent
  private String linkBackText;

  @Expose
  @Persistent
  private boolean moderationEmail;
  
  @Expose
  @Persistent
  private String approvalEmailText;

  @Expose
  @Persistent
  private String rejectionEmailText;

  @Expose
  @Persistent
  private String fromAddress;

  @Expose
  @Persistent
  private String newSubmissionAddress;
  
  public AdminConfig() {
    // Fetch default values from appengine-web.xml system props
    clientId = System.getProperty("com.google.ytd.YTClientID", "");
    developerKey = System.getProperty("com.google.ytd.YTDeveloperKey", "");  
    defaultTag = System.getProperty("com.google.ytd.BrandingTag", "");
    linkBackText = System.getProperty("com.google.ytd.LinkBackText", "");
    
    moderationEmail = false;
    approvalEmailText = System.getProperty("com.google.ytd.ApprovalEmailText", "");
    rejectionEmailText = System.getProperty("com.google.ytd.RejectionEmailText", "");
    fromAddress = System.getProperty("com.google.ytd.FromAddress", "");
    newSubmissionAddress = System.getProperty("com.google.ytd.NewSubmissionAddress", "");
    
    moderationMode = ModerationModeType.MOD_REQUIRED.ordinal();
    brandingMode = BrandingModeType.ON.ordinal();
    submissionMode = SubmissionModeType.NEW_OR_EXISTING.ordinal();
    
    loginInstruction = "Please login to your YouTube account to submit a video.<br/><br/>" +
    		"Use of this tool is governed by the " +
    		"<a href='terms.html' target='_blank'>terms of service</a>.";
    
    postSubmitMessage = "Thank you for your submission.";
    
    setUpdated(new Date());
  }
  
  public String getNewSubmissionAddress() {
    return newSubmissionAddress;
  }

  public void setNewSubmissionAddress(String newSubmissionAddress) {
    this.newSubmissionAddress = newSubmissionAddress;
  }
  
  public String getFromAddress() {
    return fromAddress;
  }

  public void setFromAddress(String fromAddress) {
    this.fromAddress = fromAddress;
  }
  
  public String getRejectionEmailText() {
    try {
      return URLDecoder.decode(rejectionEmailText, "utf-8");
    } catch (UnsupportedEncodingException e) {
      return rejectionEmailText;
    }
  }

  public void setRejectionEmailText(String rejectionEmailText) {
    this.rejectionEmailText = rejectionEmailText;
  }
  
  public boolean isModerationEmail() {
    return moderationEmail;
  }

  public void setModerationEmail(boolean moderationEmail) {
    this.moderationEmail = moderationEmail;
  }

  public String getApprovalEmailText() {
    try {
      return URLDecoder.decode(approvalEmailText, "utf-8");
    } catch (UnsupportedEncodingException e) {
      return approvalEmailText;
    }
  }

  public void setApprovalEmailText(String moderationEmailText) {
    this.approvalEmailText = moderationEmailText;
  }
  
  public String getLinkBackText() {
    return linkBackText;
  }

  public void setLinkBackText(String linkBackText) {
    this.linkBackText = linkBackText;
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
  
  public String getYouTubeAuthSubToken() {
    return youTubeAuthSubToken;
  }
  
  public void setYouTubeAuthSubToken(String youTubeAuthSubToken) {
    this.youTubeAuthSubToken = youTubeAuthSubToken;
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

  public String getPostSubmitMessage() {
    return postSubmitMessage;
  }

  public void setPostSubmitMessage(String postSubmitMessage) {
    this.postSubmitMessage = postSubmitMessage;
  }

}
