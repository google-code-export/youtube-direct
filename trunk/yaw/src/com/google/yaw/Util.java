package com.google.yaw;

import com.google.appengine.api.datastore.KeyFactory;
import com.google.yaw.model.Assignment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.servlet.http.HttpServletRequest;

public class Util {

	private static PersistenceManagerFactory pmf = null;

	public static PersistenceManagerFactory getPersistenceManagerFactory() {
		if (pmf == null) {
			pmf = JDOHelper
					.getPersistenceManagerFactory("transactions-optional");
		}
		return pmf;
	}

	public static Object persistJdo(Object entry) {
		PersistenceManager pm = Util.getPersistenceManagerFactory()
				.getPersistenceManager();
		entry = pm.makePersistent(entry);
		entry = pm.detachCopy(entry);
		pm.close();

		return entry;
	}

	public static void removeJdo(Object entry) {
		PersistenceManager pm = Util.getPersistenceManagerFactory()
				.getPersistenceManager();
		pm.deletePersistent(entry);
		pm.close();
	}
	
	/**
	 * Retrieves an Assignment from the datastore given its key.
	 * 
	 * @param key A key corresponding to an Assignment object in the datastore.
	 * @return The Assignment object whose key is specified, or null if the key is invalid.
	 */
	public static Assignment getAssignmentByKey(String key) {
	    PersistenceManager pm = Util.getPersistenceManagerFactory().getPersistenceManager();
	    
	    try {
	        return pm.getObjectById(Assignment.class, Long.parseLong(key));
	    } catch (NumberFormatException e) {
	        return null;
	    } catch (JDOObjectNotFoundException e) {
	        return null;
	    } finally {
	        pm.close();
	    }
	}
	
	/**
	 * Quick helper method to check whether an assignment key exists in the datastore.
	 * @param key A key that may correspond to an Assignment object in the datastore.
	 * @return true if the key corresponds to a valid Assignment, and false otherwise.
	 */
	public static Boolean isValidAssignmentKey(String key) {
	    Assignment assignment = Util.getAssignmentByKey(key);

	    return assignment != null;
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
		
		if (request.getQueryString().length() > 0) {
			url.append("?");
			url.append(request.getQueryString());		
		}
		
		return url.toString();
	}
	
	public static Boolean isNullOrEmpty(String input) {
	    if (input == null || input.length() <= 0) {
	        return true;
	    } else {
	        return false;
	    }
	}
}
