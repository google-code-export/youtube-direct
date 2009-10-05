package com.google.yaw.model;

import javax.jdo.annotations.Extension;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
public class UserAuthToken {

  @PrimaryKey
  @Extension(vendorName = "datanucleus", key = "gae.encoded-pk", value = "true")
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private String id = null;

  @Persistent
  private String youtubeName;
  
  @Persistent
  private String authSubToken;
  
  public UserAuthToken() {
    this.youtubeName = "";
    this.authSubToken = "";
  }
  
  public UserAuthToken(String youtubeName, String authSubToken) {
    this.youtubeName = youtubeName;
    this.authSubToken = authSubToken;
  }

  public String getId() {
    return id;
  }
  
  public String getYoutubeName() {
    return youtubeName;
  }

  public void setYoutubeName(String youtubeName) {
    this.youtubeName = youtubeName;
  }

  public String getAuthSubToken() {
    return authSubToken;
  }

  public void setAuthSubToken(String authSubToken) {
    this.authSubToken = authSubToken;
  }
}
