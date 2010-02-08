package com.google.ytd.command;

import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.inject.Inject;
import com.google.ytd.util.Util;

public class DeleteStuff extends Command {
  private static final Logger LOG = Logger.getLogger(DeleteStuff.class.getName());

  private Util util = null;
  private PersistenceManagerFactory pmf = null;

  @Inject
  public DeleteStuff(Util util, PersistenceManagerFactory pmf) {
    this.util = util;
    this.pmf = pmf;
  }

  @Override
  public JSONObject execute() throws JSONException {
    LOG.info(this.toString());
    JSONObject json = new JSONObject();

    String className = getParam("className");

    if (util.isNullOrEmpty(className)) {
      throw new IllegalArgumentException("Missing required param: className");
    }
    PersistenceManager pm = pmf.getPersistenceManager();
    try {
      Query query = pm.newQuery(Class.forName(className));
      query.deletePersistentAll();
    } catch (ClassNotFoundException e) {
      throw new IllegalArgumentException(className + " must be a valid fully-qualified class.");
    } finally {
      pm.close();
    }

    json.put("success", "true");

    return json;
  }

}
