package com.google.ytd.dao;

import java.util.List;

import com.google.ytd.model.PhotoEntry;
import com.google.ytd.model.PhotoSubmission;

public interface PhotoSubmissionDao {
  public List<PhotoSubmission> getPhotoSubmissions(String sortBy, String sortOrder);

  PhotoSubmission save(PhotoSubmission submission);

  PhotoEntry save(PhotoEntry photo);

  List<PhotoEntry> getAllPhotos(String submissionId);
}
