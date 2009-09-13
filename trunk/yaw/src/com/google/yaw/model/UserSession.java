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

  @Persistent(serialized = "true", defaultFetchGroup="true")
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
