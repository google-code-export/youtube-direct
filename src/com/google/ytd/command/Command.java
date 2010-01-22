package com.google.ytd.command;

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

	@Override
	public String toString() {
		Set<Entry<String, String>> params_ = params.entrySet();
		Iterator<Entry<String, String>> iterator = params_.iterator();
		StringBuffer str = new StringBuffer();
		while (iterator.hasNext()) {
			Entry<String, String> entry = iterator.next();
			String name = entry.getKey();
			String value = entry.getValue();
			str.append(String.format("%s=%s", name, value));
			if (iterator.hasNext()) {
				str.append(";");
			}
		}
		return str.toString();
	}
}
