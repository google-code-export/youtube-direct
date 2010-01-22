package com.google.ytd.dao;

import java.util.List;

import com.google.ytd.model.PhotoSubmission;

public interface PhotoSubmissionDao {
  public List<PhotoSubmission> getAllPhotoSubmissionsById(String assignmentId);

  public PhotoSubmission getPhotoSubmission(String photoId);
}
