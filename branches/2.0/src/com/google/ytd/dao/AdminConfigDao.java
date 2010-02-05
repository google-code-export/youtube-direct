package com.google.ytd.dao;

import com.google.ytd.model.AdminConfig;

import java.security.PrivateKey;

public interface AdminConfigDao {
  public AdminConfig getAdminConfig();

  public boolean isUploadOnly();
  public boolean allowPhotoSubmission();
  public PrivateKey getPrivateKey();

  public AdminConfig save(AdminConfig adminConfig);
}
