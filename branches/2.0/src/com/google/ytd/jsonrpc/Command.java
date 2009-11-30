package com.google.ytd.jsonrpc;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

abstract public class Command {
  private static final Logger LOG = Logger.getLogger(Command.class.getName());
  private Map<String, String> params = new HashMap<String, String>();

  abstract public JSONObject execute() throws JSONException;

  public void setParams(Map<String, String> data) {
    params = data;
  }

  public Map<String, String> getParams() {
    return params;
  }

  public String getParam(String key) {
    return this.params.get(key);
  }

  protected void printMap(Map<String, String> map) {
    Set<Entry<String, String>> params = map.entrySet();
    Iterator<Entry<String, String>> iterator = params.iterator();
    while(iterator.hasNext()) {
      Entry<String, String> entry = iterator.next();
      String name = entry.getKey();
      String value = entry.getValue();
      LOG.info(name + " = " + value);
    }
  }
}
