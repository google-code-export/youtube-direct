package com.google.ytd.guice;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.ytd.dao.AdminConfigDao;
import com.google.ytd.dao.AdminConfigDaoImpl;
import com.google.ytd.dao.AssignmentDao;
import com.google.ytd.dao.AssignmentDaoImpl;
import com.google.ytd.dao.VideoSubmissionDao;
import com.google.ytd.dao.VideoSubmissionDaoImpl;
import com.google.ytd.dao.UserAuthTokenDao;
import com.google.ytd.dao.UserAuthTokenDaoImpl;

public class ProductionModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(AdminConfigDao.class).to(AdminConfigDaoImpl.class);
    bind(AssignmentDao.class).to(AssignmentDaoImpl.class);
    bind(VideoSubmissionDao.class).to(VideoSubmissionDaoImpl.class);
    bind(UserAuthTokenDao.class).to(UserAuthTokenDaoImpl.class);
  }

  @Provides
  @Singleton
  PersistenceManagerFactory providePmf() {
    return JDOHelper.getPersistenceManagerFactory("transactions-optional");
  }
}
