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

import javax.jdo.annotations.Extension;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Model class for the YouTube user to AuthSub token mapping.
 */
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

  @Persistent
  private String clientLoginToken;  
  
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

  public void setClientLoginToken(String clientLoginToken) {
    this.clientLoginToken = clientLoginToken;
  }

  public String getClientLoginToken() {
    return clientLoginToken;
  }
}
