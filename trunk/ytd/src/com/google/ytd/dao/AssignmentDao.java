package com.google.ytd.dao;

import java.util.List;

import com.google.ytd.model.Assignment;

public interface AssignmentDao {
  public Assignment newAssignment(Assignment assignment, String playlistTitle, String channelId);

  public Assignment save(Assignment assignment);

  public Assignment getAssignmentById(long id);

  public Assignment getAssignmentById(String id);

  public long getDefaultMobileAssignmentId();

  public List<Assignment> getAssignments(String sortBy, String sortOrder, String filterType);
  
  public List<Assignment> getActiveVideoAssignments();
  
  public List<Assignment> getActivePhotoAssignments();
  
  public boolean isAssignmentPhotoEnabled(String id);
}