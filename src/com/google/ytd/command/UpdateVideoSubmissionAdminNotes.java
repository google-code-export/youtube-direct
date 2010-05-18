package com.google.ytd.command;

import java.util.Date;

import org.json.JSONObject;

import com.google.inject.Inject;
import com.google.ytd.dao.VideoSubmissionDao;
import com.google.ytd.model.VideoSubmission;
import com.google.ytd.util.Util;

public class UpdateVideoSubmissionAdminNotes extends Command {
  private VideoSubmissionDao submissionDao = null;

  @Inject
  private Util util;

  @Inject
  public UpdateVideoSubmissionAdminNotes(VideoSubmissionDao submissionDao) {
    this.submissionDao = submissionDao;
  }

  @Override
  public JSONObject execute() {
    JSONObject json = new JSONObject();
    String id = getParam("id");
    String adminNotes = getParam("adminNotes");

    if (util.isNullOrEmpty(id)) {
      throw new IllegalArgumentException("Missing required param: id");
    }
    if (util.isNullOrEmpty(adminNotes)) {
      // Essentially emptying the admin notes
      adminNotes = "";
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
