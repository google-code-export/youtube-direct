package com.google.ytd.dao;

import com.google.ytd.model.Assignment;

public interface AssignmentDao {
  public Assignment getAssignmentById(long id);
  public Assignment getAssignmentById(String id);
  public void newAssignment(Assignment assignment);
  public long getDefaultMobileAssignmentId();
}