package com.google.yaw.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.yaw.Util;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.annotations.Extension;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

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
  @Extension(vendorName = "datanucleus", key = "gae.encoded-pk", value = "true")
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  @Expose
  private String id;

  @Persistent
  @Expose
  private String description = null;

  @Persistent
  @Expose
  private String category = null;

  @Persistent
  private Date created;

  @Persistent
  private Date updated;

  public enum AssignmentStatus {
    PENDING, ACTIVE, ARCHIVED
  }

  @Persistent
  @Expose
  private AssignmentStatus status;

  /**
   * Create a new Assignment object for the datastore.
   * 
   * @param description
   * @param category
   * @param status
   */
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

  public String getId() {
    return id;
  }

  @SuppressWarnings("unchecked")
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
}
