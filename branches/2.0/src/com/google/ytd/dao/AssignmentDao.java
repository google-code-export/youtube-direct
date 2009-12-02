package com.google.ytd.dao;

import com.google.ytd.model.Assignment;

public interface AssignmentDao {

  public void newAssignment(Assignment assignment);

  @SuppressWarnings("unchecked")
  public long getDefaultMobileAssignmentId();

}