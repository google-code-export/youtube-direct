package com.google.ytd.dao;

import java.util.List;

import com.google.ytd.model.VideoSubmission;

public interface SubmissionManager {
  public List<VideoSubmission> getSubmissions(String sortBy, String sortOrder,
      String filterType);
  public void setVideoStatus(String id, String status);
  public VideoSubmission getVideoSubmission(String id);
}