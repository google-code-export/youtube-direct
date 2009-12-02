package com.google.ytd.guice;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.ytd.dao.AssignmentDao;
import com.google.ytd.dao.AssignmentDaoImpl;
import com.google.ytd.dao.SubmissionDao;
import com.google.ytd.dao.SubmissionDaoImpl;
import com.google.ytd.dao.UserAuthTokenDao;
import com.google.ytd.dao.UserAuthTokenDaoImpl;

public class ProductionModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(AssignmentDao.class).to(AssignmentDaoImpl.class);
    bind(SubmissionDao.class).to(SubmissionDaoImpl.class);
    bind(UserAuthTokenDao.class).to(UserAuthTokenDaoImpl.class);
  }

  @Provides
  @Singleton
  PersistenceManagerFactory providePmf() {
    return JDOHelper.getPersistenceManagerFactory("transactions-optional");
  }
}
