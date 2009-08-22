package com.google.yaw;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

public class Util {

	private static final Logger log = Logger.getLogger(Util.class.getName());

	private static final String DATE_TIME_PATTERN = "hh:mm:ss a MM/dd/yyyy z";

	public final static Gson GSON = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
			.setDateFormat(DATE_TIME_PATTERN).create();

	private static PersistenceManagerFactory pmf = null;

	public static PersistenceManagerFactory getPersistenceManagerFactory() {
		if (pmf == null) {
			pmf = JDOHelper.getPersistenceManagerFactory("transactions-optional");
		}
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

		Assignment entry = null;

		PersistenceManager pm = Util.getPersistenceManagerFactory().getPersistenceManager();

		String filters = "id == id_";
		Query query = pm.newQuery(Assignment.class, filters);
		query.declareParameters("String id_");
		List<Assignment> list = (List<Assignment>) query.executeWithArray(new Object[] { id });
		if (list.size() > 0) {
			entry = list.get(0);
			entry = pm.detachCopy(entry);
		}

		pm.close();

		return entry;

	}

	public static String getPostBody(HttpServletRequest req) throws IOException {
		InputStream is = req.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));

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

}
