package com.google.yaw;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.servlet.http.HttpServletRequest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.yaw.model.Assignment;
import com.google.yaw.model.AdminConfig;
import com.google.yaw.model.VideoSubmission;
import com.google.yaw.model.VideoSubmission.ModerationStatus;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class Util {

  private static final Logger log = Logger.getLogger(Util.class.getName());

  private static final String DATE_TIME_PATTERN = "EEE, d MMM yyyy HH:mm:ss Z";

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
    
    try {
      entry = pm.makePersistent(entry);
      entry = pm.detachCopy(entry);
    } finally {
      pm.close();
    }

    return entry;
  }

  public static void removeJdo(Object entry) {
    PersistenceManager pm = Util.getPersistenceManagerFactory().getPersistenceManager();
    
    try {
      pm.deletePersistent(entry);
    } finally {
      pm.close();
    }
  }

  public static void sendNotifyEmail(VideoSubmission entry, ModerationStatus newStatus,
          String sender, String additionalNote) {
    try {
      String subject;
      StringBuffer body = new StringBuffer();

      String notifyEmail = entry.getNotifyEmail();
      if (Util.isNullOrEmpty(notifyEmail)) {
        throw new IllegalArgumentException(String.format("No notifyEmail found for " +
        		"VideoSubmission id '%s'.", entry.getId()));
      }
      
      String videoUrl = entry.getVideoUrl();
      String articleUrl = entry.getArticleUrl();

      switch (newStatus) {
        //TODO: Make these strings configuration options.
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

        default:
          throw new IllegalArgumentException(String.format("Unexpected ModerationStatus: %s.",
                  newStatus.toString()));
      }

      if (!Util.isNullOrEmpty(additionalNote)) {
        body.append(additionalNote);
      }

      Properties props = new Properties();
      Session session = Session.getDefaultInstance(props, null);

      Message msg = new MimeMessage(session);
      msg.setFrom(new InternetAddress(sender, sender));
      msg.addRecipient(Message.RecipientType.TO, new InternetAddress(notifyEmail, notifyEmail));
      msg.setSubject(subject);
      msg.setText(body.toString());
      Transport.send(msg);
    } catch (IllegalArgumentException e) {
      log.log(Level.WARNING, "", e);
    } catch (MessagingException e) {
      log.log(Level.WARNING, "", e);
    } catch (UnsupportedEncodingException e) {
      log.log(Level.WARNING, "", e);
    }
  }
  
  public static Assignment getAssignmentById(long id) {
    PersistenceManager pm = Util.getPersistenceManagerFactory().getPersistenceManager();
    
    try {
      Assignment assignment = pm.getObjectById(Assignment.class, id);
      return pm.detachCopy(assignment);
    } catch (JDOObjectNotFoundException e) {
      log.log(Level.WARNING, "", e);
      return null;
    } finally {
      pm.close();
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
    try {
      return getAssignmentById(Long.parseLong(id));
    } catch (NumberFormatException e) {
      log.log(Level.WARNING, "", e);
      return null;
    }
  }
  
  @SuppressWarnings("unchecked")
  public static AdminConfig getAdminConfig() {
    AdminConfig adminConfig = null;
    
    PersistenceManager pm = Util.getPersistenceManagerFactory().getPersistenceManager();      
    
    try {
      Query query = pm.newQuery(AdminConfig.class);
      List<AdminConfig> adminConfigs = (List<AdminConfig>) query.execute();
      
      if (adminConfigs.size() > 0) {
        adminConfig = pm.detachCopy(adminConfigs.get(0));
      } else {        
        log.info("No admin config found in datastore.  Creating a new one.");
        adminConfig = new AdminConfig();        
        pm.makePersistent(adminConfig);
        adminConfig = pm.detachCopy(adminConfig);
      }
    } catch (JDOObjectNotFoundException e) {
      // this path can only occur when there is model class errors (model binary mistmatch in store)
      log.log(Level.WARNING, "Query cannot be executed against AdminConfig model class.  " + 
          "Has model class been changed?", e);
    } finally {
      pm.close();
    }
    
    return adminConfig;
  }

  public static boolean isUploadOnly() {
    boolean uploadOnly = false;
    AdminConfig adminConfig = Util.getAdminConfig();
    if (adminConfig.getSubmissionMode() == AdminConfig.SubmissionModeType.NEW_ONLY.ordinal()) {
      uploadOnly = true;
    }
    return uploadOnly;
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
   * Sorts a list and then performs a join into one large string, using the
   * delimeter specified.
   * 
   * @param strings
   *          The list of strings to sort and join.
   * @param delimeter
   *          The delimeter string to insert in between each string in the list.
   * @return A string consisting of a sorted list of strings, joined with
   *         delimeter.
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
