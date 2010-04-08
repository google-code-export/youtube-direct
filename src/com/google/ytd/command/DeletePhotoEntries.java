package com.google.ytd.command;

import org.json.JSONObject;

import com.google.inject.Inject;
import com.google.ytd.dao.PhotoSubmissionDao;
import com.google.ytd.util.Util;

public class DeletePhotoEntries extends Command {
  @Inject
  private Util util = null;

  private PhotoSubmissionDao photoSubmissionDao = null;

  @Inject
  public DeletePhotoEntries(PhotoSubmissionDao photoSubmissionDao) {
    this.photoSubmissionDao = photoSubmissionDao;
  }

  @Override
  public JSONObject execute() {
    JSONObject json = new JSONObject();
    String ids = getParam("ids");

    if (util.isNullOrEmpty(ids)) {
      throw new IllegalArgumentException("Missing required param: ids");
    }

    photoSubmissionDao.deletePhotoEntries(ids.split(","));
    return json;
  }

}
