package com.google.ytd.dao;

import com.google.ytd.model.AdminConfig;

public interface AdminConfigDao {
  public AdminConfig getAdminConfig();
  public boolean isUploadOnly();
}
