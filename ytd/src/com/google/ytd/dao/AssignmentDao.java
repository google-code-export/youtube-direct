package com.google.ytd.dao;

import java.util.List;

import com.google.ytd.model.Assignment;

public interface AssignmentDao {
  public Assignment newAssignment(Assignment assignment, String playlistTitle);

  public Assignment save(Assignment assignment);

  public Assignment getAssignmentById(long id);

  public Assignment getAssignmentById(String id);

  public long getDefaultMobileAssignmentId();

  public List<Assignment> getAssignments(String sortBy, String sortOrder, String filterType);
  
  public boolean isAssignmentPhotoEnabled(String id);
}