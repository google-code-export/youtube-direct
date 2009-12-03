package com.google.ytd.util;

import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import com.google.inject.Inject;

public class PmfUtil {
  private static final Logger log = Logger.getLogger(PmfUtil.class.getName());

  private PersistenceManagerFactory pmf = null;

  @Inject
  public PmfUtil(PersistenceManagerFactory pmf) {
    this.pmf = pmf;
  }

  public PersistenceManagerFactory getPmf() {
    return pmf;
  }

  public Object persistJdo(Object entry) {
    PersistenceManager pm = pmf.getPersistenceManager();

    try {
      entry = pm.makePersistent(entry);
      entry = pm.detachCopy(entry);
    } finally {
      pm.close();
    }

    return entry;
  }

  public void removeJdo(Object entry) {
    PersistenceManager pm = pmf.getPersistenceManager();

    try {
      pm.deletePersistent(entry);
    } finally {
      pm.close();
    }
  }
}
