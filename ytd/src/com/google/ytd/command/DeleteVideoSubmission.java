package com.google.ytd.command;

import org.json.JSONObject;

import com.google.inject.Inject;
import com.google.ytd.dao.VideoSubmissionDao;
import com.google.ytd.util.Util;

public class DeleteVideoSubmission extends Command {
  @Inject
  private Util util = null;

  private VideoSubmissionDao videoSubmissionDao = null;

  @Inject
  public DeleteVideoSubmission(VideoSubmissionDao videoSubmissionDao) {
    this.videoSubmissionDao = videoSubmissionDao;
  }

  @Override
  public JSONObject execute() {
    JSONObject json = new JSONObject();
    String id = getParam("id");

    if (util.isNullOrEmpty(id)) {
      throw new IllegalArgumentException("Missing required param: id");
    }

    videoSubmissionDao.deleteSubmission(id);
    return json;
  }

}
