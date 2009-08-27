package com.google.yaw.admin;

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

import com.google.yaw.Util;
import com.google.yaw.model.VideoSubmission;

public class FullTextIndexer extends HttpServlet {

	private static final Logger log = Logger.getLogger(FullTextIndexer.class.getName());

	private static Compass compass;
	private static CompassGps compassGps;

	static {
		compass = new CompassConfiguration().setConnection("gae://index").setSetting(
				CompassEnvironment.ExecutorManager.EXECUTOR_MANAGER_TYPE, "disabled").addScan(
				"com/google/yaw/model").buildCompass();

		PersistenceManagerFactory pmf = Util.getPersistenceManagerFactory();

		compassGps = new SingleCompassGps(compass);
		Jdo2GpsDevice gpsDevice = new Jdo2GpsDevice("appengine", pmf);
		gpsDevice.setIgnoreMirrorExceptions(true);
		gpsDevice.setMirrorDataChanges(true);
		compassGps.addGpsDevice(gpsDevice);
		compassGps.start();
		compassGps.index();
	}

	public static void reIndex() {
		compassGps.index();
	}

	public static Compass getCompass() {
		return compass;
	}

	public static void addIndex(Object object, Class indexClass) {
		compass.getSearchEngineIndexManager().releaseLocks();
		CompassIndexSession indexSession = compass.openIndexSession();
		indexSession.save(object);
		indexSession.close();
	}

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		String query = req.getParameter("q");

		CompassSearchSession search = FullTextIndexer.getCompass().openSearchSession();

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
