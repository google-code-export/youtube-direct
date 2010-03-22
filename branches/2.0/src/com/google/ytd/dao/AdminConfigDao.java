package com.google.ytd.dao;

import com.google.ytd.model.AdminConfig;

import java.security.PrivateKey;

public interface AdminConfigDao {
  public AdminConfig getAdminConfig();

  public boolean isUploadOnly();
  
  public boolean allowPhotoSubmission();
  
  public PrivateKey getPrivateKey();

  public long getMaxPhotoSize();

  public AdminConfig save(AdminConfig adminConfig);
  
  public String getLoginInstruction(String assignmentId);
  
  public String getPostSubmitMessage(String assignmentId);
}