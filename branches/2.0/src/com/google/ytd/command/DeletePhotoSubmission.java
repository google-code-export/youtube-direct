package com.google.ytd.command;

import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.inject.Inject;
import com.google.ytd.dao.PhotoSubmissionDao;
import com.google.ytd.util.Util;

public class DeletePhotoSubmission extends Command {
  private static final Logger LOG = Logger.getLogger(DeletePhotoSubmission.class.getName());

  @Inject
  private Util util = null;

  private PhotoSubmissionDao photoSubmissionDao = null;

  @Inject
  public DeletePhotoSubmission(PhotoSubmissionDao photoSubmissionDao) {
    this.photoSubmissionDao = photoSubmissionDao;
  }

  @Override
  public JSONObject execute() throws JSONException {
    LOG.info(this.toString());
    JSONObject json = new JSONObject();
    String id = getParam("id");

    if (util.isNullOrEmpty(id)) {
      throw new IllegalArgumentException("Missing required param: id");
    }

    photoSubmissionDao.deleteSubmission(id);
    return json;
  }

}
