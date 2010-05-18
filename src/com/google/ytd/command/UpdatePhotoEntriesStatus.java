package com.google.ytd.command;

import org.json.JSONObject;

import com.google.inject.Inject;
import com.google.ytd.dao.PhotoSubmissionDao;
import com.google.ytd.model.PhotoEntry;
import com.google.ytd.model.PhotoEntry.ModerationStatus;
import com.google.ytd.util.Util;

public class UpdatePhotoEntriesStatus extends Command {

  private PhotoSubmissionDao photoSubmissionDao = null;
  private Util util = null;

  @Inject
  public UpdatePhotoEntriesStatus(PhotoSubmissionDao submissionDao, Util util) {
    this.photoSubmissionDao = submissionDao;
    this.util = util;
  }

  @Override
  public JSONObject execute() {
    JSONObject json = new JSONObject();
    String ids = getParam("ids");
    String status = getParam("status");

    if (util.isNullOrEmpty(ids)) {
      throw new IllegalArgumentException("Missing required param: ids");
    }
    if (util.isNullOrEmpty(status)) {
      throw new IllegalArgumentException("Missing required param: status");
    }

    for (String id : ids.split(",")) {
      PhotoEntry entry = photoSubmissionDao.getPhotoEntry(id);
      entry.setStatus(ModerationStatus.valueOf(status.toUpperCase()));
      photoSubmissionDao.save(entry);
    }
    
    return json;
  }
}