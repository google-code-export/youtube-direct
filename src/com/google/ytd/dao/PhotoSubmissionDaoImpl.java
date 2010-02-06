package com.google.ytd.dao;

import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;

import com.google.inject.Inject;
import com.google.ytd.model.Assignment;
import com.google.ytd.model.PhotoSubmission;
import com.google.ytd.util.Util;

public class PhotoSubmissionDaoImpl implements PhotoSubmissionDao {
  @Inject
  private Util util;
  @Inject
  private PersistenceManagerFactory pmf;

  @Override
  public PhotoSubmission getPhotoSubmissionsById(String batchId) {
    PersistenceManager pm = pmf.getPersistenceManager();
    List<PhotoSubmission> submissions = null;
    PhotoSubmission submission = null;

    try {
      Query query = pm.newQuery(PhotoSubmission.class);
      query.declareParameters("String batchId_");
      String filters = "batchId == batchId_";
      query.setFilter(filters);

      submissions = (List<PhotoSubmission>) query.execute(batchId);
      if (submissions.size() > 0) {
        submission = submissions.get(0);
      }
    } finally {
      pm.close();
    }

    return submission;
  }

  @Override
  public List<PhotoSubmission> getPhotoSubmissions(String sortBy, String sortOrder) {
    PersistenceManager pm = pmf.getPersistenceManager();
    List<PhotoSubmission> submissions = null;

    try {
      Query query = pm.newQuery(Assignment.class);
      query.declareImports("import java.util.Date");
      query.setOrdering(sortBy + " " + sortOrder);

      submissions = (List<PhotoSubmission>) query.execute();
      submissions = (List<PhotoSubmission>) pm.detachCopyAll(submissions);
    } finally {
      pm.close();
    }

    return submissions;
  }

}
