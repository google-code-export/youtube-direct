package com.google.ytd.cron;

import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.ytd.model.PhotoEntry;
import com.google.ytd.util.EmailUtil;
import com.google.ytd.util.PmfUtil;

import java.util.Calendar;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Singleton
public class PurgeBlobstorePhotos extends HttpServlet {
  private static final Logger LOG = Logger.getLogger(PurgeBlobstorePhotos.class.getName());
  
  // Any PhotoEntry still in the Blobstore older than this number of hours will be purged.
  private static final int MAX_AGE_IN_HOURS = 6;

  @Inject
  private PmfUtil pmfUtil;
  
  @Inject
  private EmailUtil emailUtil;

  @SuppressWarnings("unchecked")
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) {
    LOG.info("Starting up...");

    PersistenceManager pm = pmfUtil.getPmf().getPersistenceManager();
    Query query = pm.newQuery(PhotoEntry.class);
    // I'd like to include a filter for blobKey != null here, but the datastore doesn't
    // seem to support it.
    query.setFilter("created < oldestAllowedDate");
    query.declareParameters("java.util.Date oldestAllowedDate");
    
    Calendar calendar = Calendar.getInstance();
    // The delta needs to be negative, so use 0 - MAX_AGE_IN_HOURS
    calendar.add(Calendar.HOUR_OF_DAY, 0 - MAX_AGE_IN_HOURS);
    LOG.info(calendar.getTime().toString());

    int count = 0;
    try {
      List<PhotoEntry> photoEntries = (List<PhotoEntry>) query.execute(calendar.getTime());
      for (PhotoEntry photoEntry : photoEntries) {
        if (photoEntry.getBlobKey() != null) {
          LOG.info(String.format("About to purge PhotoEntry id '%s'...", photoEntry.getId()));
          
          count++;
          
          purge(photoEntry, pm);
          
          LOG.info("Photo data for entry has been purged.");
        }
      }
    } finally {
      query.closeAll();
    }
    
    LOG.info(String.format("All done. %d PhotoEntry(s) were purged from the Blobstore.", count));
  }
  
  private void purge(PhotoEntry photoEntry, PersistenceManager pm) {
    try {
      emailUtil.sendPhotoEntryToAdmins(photoEntry);
    } finally {
      BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
      blobstoreService.delete(photoEntry.getBlobKey());
    
      photoEntry.setBlobKey(null);
      pm.makePersistent(photoEntry);
      
      LOG.info("BlobKey set to null.");
    }
  }
}
