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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.gson.annotations.Expose;

/**
 * Model class for an assignment. All submissions are associated with exactly
 * one assignment.
 */
@SuppressWarnings("serial")
@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
public class Assignment implements Serializable {

  // The default "version" of this model.
  private static int DEFAULT_SCHEMA_VERSION = 1;

  // The version of the model - used for upgrading entities if the data model
  // changes.
  @Persistent
  private int SCHEMA_VERSION;

  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  @Expose
  private Long id;

  @Persistent
  @Expose
  private String description = null;

  @Persistent
  @Expose
  private String category = null;

  @Persistent
  @Expose
  private String playlistId = "";

  @Persistent
  @Expose
  private Date created;

  @Persistent
  @Expose
  private Date updated;

  public enum AssignmentStatus {
    PENDING, ACTIVE, ARCHIVED
  }

  @Persistent
  @Expose
  private AssignmentStatus status;


  public Assignment() {
    this.SCHEMA_VERSION = DEFAULT_SCHEMA_VERSION;
    this.created = new Date();
    this.updated = this.created;
  }

  public Assignment(String description, String category, AssignmentStatus status) {
    this.description = description;
    this.category = category;
    this.status = status;
    this.SCHEMA_VERSION = DEFAULT_SCHEMA_VERSION;
    this.created = new Date();
    this.updated = this.created;
  }

  public static List<String> getAssignmentStatusNames() {
    List<String> statusNames = new ArrayList<String>();
    for (AssignmentStatus status : AssignmentStatus.values()) {
      statusNames.add(status.toString());
    }

    return statusNames;
  }

  /**
   * Gets the description of this assignment.
   *
   * @return The description of this assignment.
   */
  public String getDescription() {
    return description;
  }

  /**
   * Sets the description of this assignment.
   *
   * @param description
   *          The assignment description.
   */
  public void setDescription(String description) {
    this.description = description;
    this.updated = new Date();
  }

  /**
   * Gets the YouTube category used for videos uploaded for this assignment.
   *
   * @return The YouTube category name.
   */
  public String getCategory() {
    return category;
  }

  /**
   * Sets the YouTube category used for videos uploaded for this assignment.
   *
   * @param category
   *          The YouTube category name.
   */
  public void setCategory(String category) {
    this.category = category;
    this.updated = new Date();
  }

  /**
   * Gets the date this assignment was initially created.
   *
   * @return The date this assignment was initially created.
   */
  public Date getCreated() {
    return created;
  }

  /**
   * Gets the date this assignment was last updated.
   *
   * @return The date this assignment was last updated.
   */
  public Date getUpdated() {
    return updated;
  }

  /**
   * Update the schema version when the model changes.
   *
   * @param version
   *          The new version.
   */
  public void setSchemaVersion(int version) {
    this.SCHEMA_VERSION = version;
  }

  /**
   * Gets the current schema version.
   *
   * @return The current schema version of this entity
   */
  public int getSchemaVersion() {
    return SCHEMA_VERSION;
  }

  public static int getDEFAULT_SCHEMA_VERSION() {
    return DEFAULT_SCHEMA_VERSION;
  }

  public static void setDEFAULT_SCHEMA_VERSION(int default_schema_version) {
    DEFAULT_SCHEMA_VERSION = default_schema_version;
  }

  public int getSCHEMA_VERSION() {
    return SCHEMA_VERSION;
  }

  public void setSCHEMA_VERSION(int schema_version) {
    SCHEMA_VERSION = schema_version;
  }

  public AssignmentStatus getStatus() {
    return status;
  }

  public void setStatus(AssignmentStatus status) {
    this.status = status;
  }

  public Long getId() {
    return id;
  }

  public String getPlaylistId() {
    return playlistId;
  }

  public void setPlaylistId(String playlistId) {
    this.playlistId = playlistId;
  }

  /*
  @SuppressWarnings("unchecked")
  //TODO: This should probably be calculated periodically and persisted instead of dynamically.
  public int getSubmissionCount() {
    PersistenceManagerFactory pmf = Util.getPersistenceManagerFactory();
    PersistenceManager pm = pmf.getPersistenceManager();

    Query query = pm.newQuery(VideoSubmission.class, "assignmentId == assignmentIdParam");
    query.declareParameters("String assignmentIdParam");
    List<VideoSubmission> submissions = (List<VideoSubmission>) query.execute(id);
    int size = submissions.size();

    pm.close();

    return size;
  }
  */
}
