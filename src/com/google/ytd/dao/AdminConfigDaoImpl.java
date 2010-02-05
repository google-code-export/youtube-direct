package com.google.ytd.dao;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;

import com.google.inject.Inject;
import com.google.ytd.model.AdminConfig;

public class AdminConfigDaoImpl implements AdminConfigDao {
  private static final Logger LOG = Logger.getLogger(AdminConfigDaoImpl.class.getName());

  private PersistenceManagerFactory pmf = null;

  @Inject
  public AdminConfigDaoImpl(PersistenceManagerFactory pmf) {
    this.pmf = pmf;
  }

  @SuppressWarnings("unchecked")
  public AdminConfig getAdminConfig() {
    AdminConfig adminConfig = null;

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
      LOG.log(Level.WARNING, "Query cannot be executed against AdminConfig model class. "
          + "Has model class been changed?", e);
    } finally {
      pm.close();
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
  
  public boolean allowPhotoSubmission() {
    //TODO: Add some logic here.
    return true;
  }
  
  public PrivateKey getPrivateKey() {
    byte[] privateKeyBytes = getAdminConfig().getPrivateKeyBytes();
    if (privateKeyBytes != null && privateKeyBytes.length > 0) {
      try {
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
        
        return privateKey;
      } catch(InvalidKeySpecException e) {
        LOG.log(Level.WARNING, "", e);
      } catch (NoSuchAlgorithmException e) {
        LOG.log(Level.WARNING, "", e);
      }
    }

    return null;
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
