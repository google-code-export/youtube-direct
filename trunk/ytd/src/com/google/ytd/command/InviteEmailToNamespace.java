package com.google.ytd.command;

import com.google.appengine.api.NamespaceManager;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.mail.MailService;
import com.google.appengine.api.mail.MailServiceFactory;
import com.google.appengine.api.mail.MailService.Message;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.apphosting.api.ApiProxy;
import com.google.inject.Inject;
import com.google.ytd.util.Util;

import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

public class InviteEmailToNamespace extends Command {
  private static final Logger LOG = Logger.getLogger(InviteEmailToNamespace.class.getName());
  @Inject
  private Util util;

  @Override
  public JSONObject execute() {
    String email = getParam("email");
    if (util.isNullOrEmpty(email)) {
      throw new IllegalArgumentException("Required parameter 'email' is missing.");
    }
    try {
      new InternetAddress(email, true);
      if (!email.contains("@")) {
        throw new AddressException();
      }
    } catch (AddressException e) {
      throw new IllegalArgumentException(String.format("'%s' is not a valid email address.",
        email));
    }
    email = email.toLowerCase();
    
    String namespace = getParam("namespace");
    if (util.isNullOrEmpty(namespace)) {
      throw new IllegalArgumentException("Required parameter 'namespace' is missing.");
    }
    if (namespace.matches(".*\\W.*")) {
      throw new IllegalArgumentException("Parameter 'namespace' can only contain letters and " +
      		"numbers.");
    }
    
    NamespaceManager.set("nsadmin");
    if (isEmailAlreadyPresentForNamespace(email, namespace)) {
      throw new IllegalArgumentException(String.format("'%s' is already associated with '%s'.",
        email, namespace));
    }
    
    Entity entity = addEntityToDatastore(email, namespace);
    if (entity == null) {
      throw new IllegalStateException("Couldn't add entry to datastore.");
    }
    
    UserService userService = UserServiceFactory.getUserService();
    User currentUser = userService.getCurrentUser();
    
    try {
      sendEmailInvitation(currentUser.getEmail(), email,
        (String) entity.getProperty("confirmation"));
    } catch (IOException e) {
      LOG.log(Level.WARNING, "", e);
      throw new IllegalStateException(e);
    }
    
    LOG.info(String.format("Successfully invited %s to administer namespace '%s'.",
      email, namespace));

    return new JSONObject();
  }
  
  private boolean isEmailAlreadyPresentForNamespace(String email, String namespace) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Query query = new Query("NamespaceToUserMapping");
    query.addFilter("namespace", Query.FilterOperator.EQUAL, namespace);
    query.addFilter("email", Query.FilterOperator.EQUAL, email);
    PreparedQuery preparedQuery = datastore.prepare(query);
    int count = preparedQuery.countEntities(FetchOptions.Builder.withLimit(1));
    return count > 0;
  }
  
  private Entity addEntityToDatastore(String email, String namespace) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Entity entity = new Entity("NamespaceToUserMapping");
    entity.setProperty("namespace", namespace);
    entity.setProperty("email", email);
    entity.setProperty("confirmed", false);
    entity.setProperty("confirmation", UUID.randomUUID().toString());
    datastore.put(entity);
    
    return entity;
  }
  
  private void sendEmailInvitation(String fromAddress, String toAddress, String confirmationCode) throws IOException {
    String url = String.format("https://%s/AcceptInvite?confirmation=%s",
      ApiProxy.getCurrentEnvironment().getAttributes().get("com.google.appengine.runtime.default_version_hostname"),
      URLEncoder.encode(confirmationCode, "UTF-8"));
    String body = String.format("You've been invited to administer an instance of " +
            "YouTube Direct. To accept this invitation, please visit %s", url);
    
    MailService mailService = MailServiceFactory.getMailService();
    Message message = new Message();

    message.setSubject("Invitation to Administer YouTube Direct");
    message.setSender(fromAddress);
    message.setTo(toAddress);
    message.setTextBody(body);
    mailService.send(message);
  }
}