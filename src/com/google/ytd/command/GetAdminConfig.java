package com.google.ytd.command;

import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.inject.Inject;
import com.google.ytd.dao.AdminConfigDao;
import com.google.ytd.model.AdminConfig;
import com.google.ytd.util.Util;

public class GetAdminConfig extends Command {
	private AdminConfigDao adminConfigDao = null;

	private static final Logger LOG = Logger.getLogger(GetAdminConfig.class.getName());

	@Inject
	private Util util;

	@Inject
	public GetAdminConfig(AdminConfigDao adminConfigDao) {
		this.adminConfigDao = adminConfigDao;
	}

	@Override
	public JSONObject execute() throws JSONException {
		AdminConfig adminConfig = adminConfigDao.getAdminConfig();
		if (adminConfig == null) {
			throw new IllegalStateException("No admin config can be found.");
		}
		JSONObject json = new JSONObject();
		json.put("result", new JSONObject(util.toJson(adminConfig)));
		return json;
	}

}
