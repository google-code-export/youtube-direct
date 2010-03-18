package com.google.ytd.dao;

import java.util.List;

import com.google.ytd.model.PhotoEntry;
import com.google.ytd.model.PhotoSubmission;

public interface PhotoSubmissionDao {
  public List<PhotoSubmission> getPhotoSubmissions(String sortBy, String sortOrder);

  public PhotoSubmission save(PhotoSubmission submission);

  public PhotoEntry save(PhotoEntry photo);
 
  public List<PhotoEntry> getAllPhotos(String submissionId);

  public PhotoSubmission getSubmissionById(String id);
}
