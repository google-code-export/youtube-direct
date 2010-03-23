package com.google.ytd.command;

import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.inject.Inject;
import com.google.ytd.dao.PhotoSubmissionDao;
import com.google.ytd.model.PhotoEntry;
import com.google.ytd.model.PhotoEntry.ModerationStatus;
import com.google.ytd.util.Util;

public class UpdatePhotoEntryStatus extends Command {
  private static final Logger LOG = Logger.getLogger(UpdatePhotoEntryStatus.class.getName());

  private PhotoSubmissionDao submissionDao = null;
  private Util util = null;

  @Inject
  public UpdatePhotoEntryStatus(PhotoSubmissionDao submissionDao, Util util) {
    this.submissionDao = submissionDao;
    this.util = util;
  }

  @Override
  public JSONObject execute() throws JSONException {
    LOG.info(this.toString());
    JSONObject json = new JSONObject();
    String id = getParam("id");
    String status = getParam("status");

    if (util.isNullOrEmpty(id)) {
      throw new IllegalArgumentException("Missing required param: id");
    }
    if (util.isNullOrEmpty(status)) {
      throw new IllegalArgumentException("Missing required param: status");
    }

    PhotoEntry entry = submissionDao.getPhotoEntry(id);

    ModerationStatus newStatus = ModerationStatus.valueOf(status.toUpperCase());
    ModerationStatus currentStatus = entry.getStatus();

    entry.setStatus(newStatus);

    return json;
  }
}