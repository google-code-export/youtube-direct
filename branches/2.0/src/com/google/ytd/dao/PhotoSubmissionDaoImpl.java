package com.google.ytd.dao;

import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;

import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.inject.Inject;
import com.google.ytd.model.PhotoEntry;
import com.google.ytd.model.PhotoSubmission;
import com.google.ytd.util.Util;

public class PhotoSubmissionDaoImpl implements PhotoSubmissionDao {
  @Inject
  private Util util;
  @Inject
  private PersistenceManagerFactory pmf;

  @Override
  public List<PhotoSubmission> getPhotoSubmissions(String sortBy, String sortOrder) {
    PersistenceManager pm = pmf.getPersistenceManager();
    List<PhotoSubmission> submissions = null;

    try {
      Query query = pm.newQuery(PhotoSubmission.class);
      query.declareImports("import java.util.Date");
      query.setOrdering(sortBy + " " + sortOrder);

      submissions = (List<PhotoSubmission>) query.execute();
      submissions = (List<PhotoSubmission>) pm.detachCopyAll(submissions);
    } finally {
      pm.close();
    }

    return submissions;
  }

  @Override
  public List<PhotoEntry> getAllPhotos(String submissionId) {
    PersistenceManager pm = pmf.getPersistenceManager();
    List<PhotoEntry> photos = null;

    try {
      Query query = pm.newQuery(PhotoEntry.class);
      query.declareParameters("String submissionId_");
      String filters = "submissionId == submissionId_";
      query.setFilter(filters);
      photos = (List<PhotoEntry>) query.execute(submissionId);
      photos = (List<PhotoEntry>) pm.detachCopyAll(photos);
    } finally {
      pm.close();
    }

    return photos;
  }

  @Override
  public PhotoSubmission save(PhotoSubmission submission) {
    PersistenceManager pm = pmf.getPersistenceManager();
    try {
      pm.makePersistent(submission);
      submission = pm.detachCopy(submission);
    } finally {
      pm.close();
    }
    return submission;
  }

  @Override
  public PhotoEntry save(PhotoEntry photo) {
    PersistenceManager pm = pmf.getPersistenceManager();
    try {
      pm.makePersistent(photo);
      photo = pm.detachCopy(photo);
    } finally {
      pm.close();
    }
    return photo;
  }

  @Override
  public PhotoSubmission getSubmissionById(String id) {
    PersistenceManager pm = pmf.getPersistenceManager();
    PhotoSubmission submission = null;

    try {
      submission = (PhotoSubmission) pm.getObjectById(PhotoSubmission.class, id);
    } finally {
      pm.close();
    }

    return submission;
  }
  
  @Override
  public void deletePhotoEntries(String[] ids) {    
    for (String id : ids) {
      deletePhotoEntry(id);
    }
  }
  
  @Override
  public void deletePhotoEntry(String id) {
    PersistenceManager pm = pmf.getPersistenceManager();
    PhotoEntry entry = null;
    try {                  
      entry = (PhotoEntry) pm.getObjectById(PhotoEntry.class, id);      
      
      // Update the photo count of the corresponding submission
      PhotoSubmission photoSubmission = this.getSubmissionById(entry.getSubmissionId());
      photoSubmission.setNumberOfPhotos(photoSubmission.getNumberOfPhotos() - 1);      
      this.save(photoSubmission);
      
      // Delete the persistent entry record of this image
      pm.deletePersistent(entry);
      
      // Delete the image binary from blob store
      BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
      blobstoreService.delete(entry.getBlobKey());      
    } finally {
      pm.close();
    }        
  }

  @Override
  public PhotoEntry getPhotoEntry(String id) {
    PersistenceManager pm = pmf.getPersistenceManager();
    PhotoEntry entry = null;

    try {
      entry = (PhotoEntry) pm.getObjectById(PhotoEntry.class, id);
    } finally {
      pm.close();
    }

    return entry;
  }

}
