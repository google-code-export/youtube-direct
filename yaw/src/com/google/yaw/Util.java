package com.google.yaw;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.servlet.http.HttpServletRequest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.yaw.model.Assignment;
import com.google.yaw.model.VideoSubmission;
import com.google.yaw.model.VideoSubmission.ModerationStatus;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.compass.core.Compass;
import org.compass.core.config.CompassConfiguration;
import org.compass.core.config.CompassEnvironment;
import org.compass.gps.CompassGps;
import org.compass.gps.device.jdo.Jdo2GpsDevice;
import org.compass.gps.impl.SingleCompassGps;

public class Util {

	private static final Logger log = Logger.getLogger(Util.class.getName());

	private static final String DATE_TIME_PATTERN = "hh:mm:ss a MM/dd/yyyy z";

	public final static Gson GSON = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
			.setDateFormat(DATE_TIME_PATTERN).create();

	private static PersistenceManagerFactory pmf = null;
	
	static {
		pmf = JDOHelper.getPersistenceManagerFactory("transactions-optional");		
	}
	
	public static PersistenceManagerFactory getPersistenceManagerFactory() {
		return pmf;
	}

	public static Object persistJdo(Object entry) {
		PersistenceManager pm = Util.getPersistenceManagerFactory().getPersistenceManager();
		entry = pm.makePersistent(entry);
		entry = pm.detachCopy(entry);
		pm.close();

		return entry;
	}

	public static void removeJdo(Object entry) {
		PersistenceManager pm = Util.getPersistenceManagerFactory().getPersistenceManager();
		pm.deletePersistent(entry);
		pm.close();
	}

	public static void sendNotifyEmail(VideoSubmission entry, ModerationStatus newStatus, 
			String sender, String additionalNote) {
				
		String subject = "";
		StringBuffer body = new StringBuffer();					
		
		String notifyEmail = entry.getNotifyEmail();
		String videoUrl = entry.getVideoUrl();
		String articleUrl = entry.getArticleUrl();
		
		switch (newStatus) {
			case APPROVED:
				subject = "your submission is approved.";
				body.append(String.format("your video response (%s) has been approved for this article %s.", 
						videoUrl, articleUrl));							
				break;
			case REJECTED:
				subject = "your submission is rejected.";
				body.append(String.format("your video response (%s) has been rejected for this article %s.", 
						videoUrl, articleUrl));
				break;		
		}

		if (additionalNote != null && !additionalNote.equals("")) {
			body.append(additionalNote);					
		}		
		
		Properties props = new Properties();
    Session session = Session.getDefaultInstance(props, null);

    try {
        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(sender, sender));
        msg.addRecipient(Message.RecipientType.TO,
                         new InternetAddress(notifyEmail, notifyEmail));
        msg.setSubject(subject);
        msg.setText(body.toString());
        Transport.send(msg);

    } catch (Exception e) {
        // ...
    } 
		
	}
	
	/**
	 * Retrieves an Assignment from the datastore given its id.
	 * 
	 * @param id
	 *          An ID corresponding to an Assignment object in the datastore.
	 * @return The Assignment object whose id is specified, or null if the id is
	 *         invalid.
	 */
	public static Assignment getAssignmentById(String id) {
		log.warning(id);
		PersistenceManager pm = Util.getPersistenceManagerFactory().getPersistenceManager();
    Assignment assignment = pm.getObjectById(Assignment.class, id);
    if (assignment != null) {
      assignment = pm.detachCopy(assignment);
    }
    
    pm.close();
    
    return assignment;

	}

	public static String getPostBody(HttpServletRequest req) throws IOException {
		InputStream is = req.getInputStream();

		StringBuffer body = new StringBuffer();
		String line = null;
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		while ((line = br.readLine()) != null) {
			body.append(line);
			body.append("\n");
		}
		return body.toString();
	}

	public static String getSelfUrl(HttpServletRequest request) {
		StringBuffer url = new StringBuffer();

		url.append(request.getRequestURL());
		String queryString = request.getQueryString();
		if (!Util.isNullOrEmpty(queryString)) {
			url.append("?");
			url.append(queryString);
		}

		return url.toString();
	}

	public static boolean isNullOrEmpty(String input) {
		if (input == null || input.length() <= 0) {
			return true;
		} else {
			return false;
		}
	}

	public static String toJson(Object o) {
		return GSON.toJson(o);
	}
	
	/**
	 * Sorts a list and then performs a join into one large string, using the delimeter specified.
	 * @param strings The list of strings to sort and join.
	 * @param delimeter The delimeter string to insert in between each string in the list.
	 * @return A string consisting of a sorted list of strings, joined with delimeter.
	 */
	public static String sortedJoin(List<String> strings, String delimeter) {
	  Collections.sort(strings);
	  
	  StringBuffer tempBuffer = new StringBuffer();
	  for (int i = 0; i < strings.size(); i++) {
	    tempBuffer.append(strings.get(i));
	    if (i < strings.size() - 1) {
	      tempBuffer.append(delimeter);
	    }
	  }
	  
	  return tempBuffer.toString();
	}
}
