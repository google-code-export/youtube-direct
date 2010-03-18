package com.google.ytd.command;

import java.util.Date;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.inject.Inject;
import com.google.ytd.dao.PhotoSubmissionDao;
import com.google.ytd.model.PhotoSubmission;
import com.google.ytd.util.Util;

public class UpdatePhotoSubmissionAdminNotes extends Command {
  private static final Logger LOG = Logger.getLogger(UpdatePhotoSubmissionAdminNotes.class
      .getName());

  private PhotoSubmissionDao photoSubmissionDao = null;

  @Inject
  private Util util;

  @Inject
  public UpdatePhotoSubmissionAdminNotes(PhotoSubmissionDao photoSubmissionDao) {
    this.photoSubmissionDao = photoSubmissionDao;
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
      // Essentially emptying the admin notes
      adminNotes = "";
    }

    PhotoSubmission submission = photoSubmissionDao.getSubmissionById(id);

    if (submission == null) {
      throw new IllegalArgumentException("The input photo submission id cannot be located.");
    }

    submission.setAdminNotes(adminNotes);
    submission.setUpdated(new Date());
    photoSubmissionDao.save(submission);

    return json;
  }
}
