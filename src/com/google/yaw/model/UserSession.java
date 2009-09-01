package com.google.yaw.model;

import java.util.HashMap;
import java.util.Map;

import javax.jdo.annotations.Extension;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
public class UserSession {

  @PrimaryKey
  @Extension(vendorName = "datanucleus", key = "gae.encoded-pk", value = "true")
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private String id = null;

  @Persistent
  private String youTubeName = null;

  @Persistent
  private String authSubToken = null;

  @Persistent
  private String assignmentId = null;

  @Persistent
  private String articleUrl = null;

  @Persistent
  private String selfUrl = null;

  @Persistent
  private String videoTitle = null;

  @Persistent
  private String videoDescription = null;

  @Persistent
  private String videoLocation = null;

  @Persistent
  private String videoTagList = null;

  @Persistent
  private String email = null;

  @Persistent(serialized = "true")
  private Map<String, String> metaDataMap = null;  
  
  public UserSession() {
    this.metaDataMap = new HashMap<String, String>();
  }  
  
  public void setId(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }  
  
  public Map<String, String> getMetaDataMap() {
    if (this.metaDataMap == null) {      
      this.metaDataMap = new HashMap<String, String>();
    }
    return metaDataMap;
  }
  
  public void addMetaData(String key, String value) {
    getMetaDataMap().put(key, value);
  }
  
  public String getMetaData(String key) {
    return getMetaDataMap().get(key);
  }
}
