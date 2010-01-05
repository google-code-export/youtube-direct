package com.google.ytd.dao;

import com.google.ytd.model.Assignment;

public interface AssignmentDao {
  public Assignment newAssignment(Assignment assignment);
  public Assignment save(Assignment assignment);
  public Assignment getAssignmentById(long id);
  public Assignment getAssignmentById(String id);
  public long getDefaultMobileAssignmentId();
}