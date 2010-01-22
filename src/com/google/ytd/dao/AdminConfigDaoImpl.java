package com.google.ytd.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheManager;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;

import com.google.appengine.api.memcache.stdimpl.GCacheFactory;
import com.google.inject.Inject;
import com.google.ytd.model.AdminConfig;

public class AdminConfigDaoImpl implements AdminConfigDao {
  private static final Logger LOG = Logger.getLogger(AdminConfigDaoImpl.class.getName());

  private PersistenceManagerFactory pmf = null;

  // CONSTANTS
  private static final int CACHE_EXPIRATION = 60 * 60 * 24 * 7;

  @Inject
  public AdminConfigDaoImpl(PersistenceManagerFactory pmf) {
    this.pmf = pmf;
  }

  @SuppressWarnings("unchecked")
  public AdminConfig getAdminConfig() {
    AdminConfig adminConfig = null;

    // Attempt to read admin config from memcache.
    Cache cache = null;
    try {
      Map cacheProperties = new HashMap();
      cacheProperties.put(GCacheFactory.EXPIRATION_DELTA, CACHE_EXPIRATION);
      cache = CacheManager.getInstance().getCacheFactory().createCache(cacheProperties);
      adminConfig = (AdminConfig) cache.get(AdminConfig.getCacheKey());

      if (adminConfig != null) {
        return adminConfig;
      }
    } catch (CacheException e) {
      LOG.log(Level.WARNING, "", e);
    }

    PersistenceManager pm = pmf.getPersistenceManager();

    try {
      Query query = pm.newQuery(AdminConfig.class);
      List<AdminConfig> adminConfigs = (List<AdminConfig>) query.execute();
      if (adminConfigs.size() > 0) {
        adminConfig = pm.detachCopy(adminConfigs.get(0));
      } else {
        LOG.info("No admin config found in datastore.  Creating a new one.");
        adminConfig = new AdminConfig();
        pm.makePersistent(adminConfig);
        adminConfig = pm.detachCopy(adminConfig);
      }
    } catch (JDOObjectNotFoundException e) {
      // this path can only occur when there is model class errors (model binary
      // mistmatch in store)
      LOG.log(Level.WARNING, "Query cannot be executed against AdminConfig model class.  "
          + "Has model class been changed?", e);
    } finally {
      pm.close();
    }

    if (cache != null) {
      cache.put(AdminConfig.getCacheKey(), adminConfig);
    }

    return adminConfig;
  }

  public boolean isUploadOnly() {
    boolean uploadOnly = false;
    AdminConfig adminConfig = getAdminConfig();
    if (adminConfig.getSubmissionMode() == AdminConfig.SubmissionModeType.NEW_ONLY.ordinal()) {
      uploadOnly = true;
    }
    return uploadOnly;
  }

  @Override
  public AdminConfig save(AdminConfig adminConfig) {
    PersistenceManager pm = pmf.getPersistenceManager();
    try {
      adminConfig = pm.makePersistent(adminConfig);
      adminConfig = pm.detachCopy(adminConfig);
    } finally {
      pm.close();
    }
    return adminConfig;
  }
}
