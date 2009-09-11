package com.google.yaw.model;

import java.io.Serializable;

import javax.jdo.annotations.Extension;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Model class for all web application settings.
 */
@PersistenceCapable(detachable = "true")
public class Settings implements Serializable {
  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Long id;
  
  @Persistent
  private String developerKey;
  
  @Persistent
  private String clientId;
  

  public Settings() {
    developerKey = "";
    clientId = "";
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
}
