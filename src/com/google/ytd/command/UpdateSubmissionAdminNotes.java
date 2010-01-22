package com.google.ytd.command;

import java.util.Date;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.inject.Inject;
import com.google.ytd.dao.SubmissionDao;
import com.google.ytd.model.VideoSubmission;
import com.google.ytd.util.Util;

public class UpdateSubmissionAdminNotes extends Command {
	private static final Logger LOG = Logger.getLogger(UpdateSubmissionAdminNotes.class.getName());

	private SubmissionDao submissionDao = null;

	@Inject
	private Util util;

	@Inject
	public UpdateSubmissionAdminNotes(SubmissionDao submissionDao) {
		this.submissionDao = submissionDao;
	}

	@Override
	public JSONObject execute() throws JSONException {
		LOG.info(this.toString());
		JSONObject json = new JSONObject();
		String id = getParam("id");
		String adminNotes = getParam("adminNotes");

		if (util.isNullOrEmpty(id)) {
			throw new IllegalArgumentException("Missing required param: id");
		}
		if (util.isNullOrEmpty(adminNotes)) {
			throw new IllegalArgumentException("Missing required param: adminNotes");
		}

		VideoSubmission submission = submissionDao.getSubmissionById(id);

		if (submission == null) {
			throw new IllegalArgumentException("The input video id cannot be located.");
		}

		submission.setAdminNotes(adminNotes);
		submission.setUpdated(new Date());
		submissionDao.save(submission);

		return json;
	}
}
