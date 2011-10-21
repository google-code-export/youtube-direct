package com.google.ytd.command;

import com.google.appengine.api.NamespaceManager;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.inject.Inject;
import com.google.ytd.util.Util;

import org.json.JSONObject;

public class RemoveEmailFromNamespace extends Command {
  @Inject
  private Util util;

  @Override
  public JSONObject execute() {
    JSONObject json = new JSONObject();
    
    String email = getParam("email");
    if (util.isNullOrEmpty(email)) {
      throw new IllegalStateException("Required parameter 'email' is missing.");
    }
    
    String namespace = getParam("namespace");
    if (util.isNullOrEmpty(namespace)) {
      throw new IllegalStateException("Required parameter 'namespace' is missing.");
    }
    
    NamespaceManager.set("nsadmin");
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Query query = new Query("NamespaceToUserMapping");
    query.addFilter("namespace", Query.FilterOperator.EQUAL, namespace);
    query.addFilter("email", Query.FilterOperator.EQUAL, email);
    PreparedQuery preparedQuery = datastore.prepare(query);

    for (Entity entity : preparedQuery.asIterable()) {
      datastore.delete(entity.getKey());
    }
    
    util.removeFromCache(namespace);

    return new JSONObject();
  }
}