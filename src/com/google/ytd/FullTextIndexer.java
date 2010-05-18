/* Copyright (c) 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.ytd;

import java.io.IOException;
import java.util.logging.Logger;

import javax.jdo.PersistenceManagerFactory;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.compass.core.Compass;
import org.compass.core.CompassHits;
import org.compass.core.CompassIndexSession;
import org.compass.core.CompassSearchSession;
import org.compass.core.Resource;
import org.compass.core.config.CompassConfiguration;
import org.compass.core.config.CompassEnvironment;
import org.compass.gps.CompassGps;
import org.compass.gps.device.jdo.Jdo2GpsDevice;
import org.compass.gps.impl.SingleCompassGps;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.ytd.model.VideoSubmission;
import com.google.ytd.util.Util;

/**
 * Servlet to handle full text indexing of datastore properties.
 * 
 * Not currently used, but may be in the future.
 */
@Singleton
public class FullTextIndexer extends HttpServlet {
  @Inject
  private Util util;
  @Inject
  private PersistenceManagerFactory pmf;

  private static final Logger log = Logger.getLogger(FullTextIndexer.class.getName());

  private static Compass compass;
  private static CompassGps compassGps;

  public FullTextIndexer() {
    compass = new CompassConfiguration().setConnection("gae://index").setSetting(
        CompassEnvironment.ExecutorManager.EXECUTOR_MANAGER_TYPE, "disabled").addScan(
        "com.google.ytd/model").buildCompass();

    compassGps = new SingleCompassGps(compass);
    Jdo2GpsDevice gpsDevice = new Jdo2GpsDevice("appengine", pmf);
    gpsDevice.setIgnoreMirrorExceptions(true);
    gpsDevice.setMirrorDataChanges(true);
    compassGps.addGpsDevice(gpsDevice);
    compassGps.start();
    compassGps.index();
  }

  public void reIndex() {
    compassGps.index();
  }

  public Compass getCompass() {
    return compass;
  }

  public void addIndex(Object object, Class indexClass) {
    compass.getSearchEngineIndexManager().releaseLocks();
    CompassIndexSession indexSession = compass.openIndexSession();
    indexSession.save(object);
    indexSession.close();
  }

  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

    String query = req.getParameter("q");

    CompassSearchSession search = getCompass().openSearchSession();

    CompassHits hits = search.find(query);

    for (int i = 0; i < hits.length(); i++) {
      VideoSubmission entry = (VideoSubmission) hits.data(i);
      Resource resource = hits.resource(i);
      log.warning("title = " + entry.getVideoTitle());
      log.warning("tags = " + entry.getVideoTags());
    }

    search.close();

    resp.getWriter().print("size: " + hits.length());
  }

  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    log.warning("task invoked!");
  }

  /*
   * public void testTaskQueue() { Queue queue = QueueFactory.getDefaultQueue();
   * 
   * TaskOptions taskOptions = TaskOptions.Builder.withDefaults();
   * 
   * taskOptions = taskOptions.url("/test"); taskOptions =
   * taskOptions.method(Method.POST);
   * 
   * queue.add(taskOptions); }
   */
}
