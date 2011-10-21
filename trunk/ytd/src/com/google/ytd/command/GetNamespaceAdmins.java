package com.google.ytd.command;

import com.google.appengine.api.NamespaceManager;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.inject.Inject;
import com.google.ytd.util.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class GetNamespaceAdmins extends Command {
  @Inject
  private Util util;

  @Override
  public JSONObject execute() throws JSONException {
    JSONObject json = new JSONObject();
    
    String namespace = getParam("namespace");
    String confirmed = getParam("confirmed");
    
    NamespaceManager.set("nsadmin");
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Query query = new Query("NamespaceToUserMapping");

    if (!util.isNullOrEmpty(namespace)) {
      query.addFilter("namespace", Query.FilterOperator.EQUAL, namespace);
    }
    
    if (!util.isNullOrEmpty(confirmed)) {
      if (confirmed.equalsIgnoreCase("true")) {
        query.addFilter("confirmed", Query.FilterOperator.EQUAL, true);
      }
      
      if (confirmed.equalsIgnoreCase("false")) {
        query.addFilter("confirmed", Query.FilterOperator.EQUAL, false);
      }
    }
    
    PreparedQuery preparedQuery = datastore.prepare(query);

    for (Entity entity : preparedQuery.asIterable()) {
      Map<String, Object> properties = entity.getProperties();
      json.accumulate((String) properties.get("namespace"), properties);
    }

    return json;
  }
}