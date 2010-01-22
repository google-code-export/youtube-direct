package com.google.ytd.command;

import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.inject.Inject;
import com.google.ytd.dao.AdminConfigDao;
import com.google.ytd.dao.AssignmentDao;
import com.google.ytd.model.Assignment;
import com.google.ytd.model.Assignment.AssignmentStatus;
import com.google.ytd.util.Util;

public class NewAssignment extends Command {
	private AssignmentDao assignmentDao = null;
	private AdminConfigDao adminConfigDao = null;

	private static final Logger LOG = Logger.getLogger(NewAssignment.class.getName());

	@Inject
	private Util util;

	@Inject
	public NewAssignment(AssignmentDao assignmentDao, AdminConfigDao adminConfigDao) {
		this.assignmentDao = assignmentDao;
		this.adminConfigDao = adminConfigDao;
	}

	@Override
	public JSONObject execute() throws JSONException {
		LOG.info(this.toString());
		JSONObject json = new JSONObject();
		String status = getParam("status");
		String description = getParam("description");
		String category = getParam("category");

		if (util.isNullOrEmpty(description)) {
			throw new IllegalArgumentException("Missing required param: description");
		}
		if (util.isNullOrEmpty(status)) {
			throw new IllegalArgumentException("Missing required param: status");
		}

		if (util.isNullOrEmpty(category)) {
			throw new IllegalArgumentException("Missing required param: category");
		}

		Assignment assignment = new Assignment();
		assignment.setStatus(AssignmentStatus.valueOf(status.toUpperCase()));
		assignment.setDescription(description);
		assignment.setCategory(category.toUpperCase());

		assignment = assignmentDao.newAssignment(assignment);

		json.put("id", assignment.getId());
		json.put("playlistId", assignment.getPlaylistId());

		return json;
	}
}
