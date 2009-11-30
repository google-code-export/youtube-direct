package com.google.ytd.guice;

import javax.jdo.PersistenceManagerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.ytd.Util;
import com.google.ytd.dao.SubmissionManager;
import com.google.ytd.dao.SubmissionManagerImpl;

public class ProductionModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(SubmissionManager.class).to(SubmissionManagerImpl.class);
  }

  @Provides
  @Singleton
  PersistenceManagerFactory providePmf() {
    //return JDOHelper.getPersistenceManagerFactory("transactions-optional");
    return Util.getPersistenceManagerFactory();
  }
}
