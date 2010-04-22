package com.google.ytd.command;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.inject.Inject;
import com.google.ytd.dao.PhotoSubmissionDao;
import com.google.ytd.model.PhotoEntry;
import com.google.ytd.util.Util;

public class GetAllPhotoEntries extends Command {
  @Inject
  private Util util = null;

  private PhotoSubmissionDao photoSubmissionDao = null;

  @Inject
  public GetAllPhotoEntries(PhotoSubmissionDao photoSubmissionDao) {
    this.photoSubmissionDao = photoSubmissionDao;
  }

  @Override
  public JSONObject execute() throws JSONException {
    JSONObject json = new JSONObject();
    List<PhotoEntry> photos = null;

    String submissionId = getParam("submissionId");

    if (util.isNullOrEmpty(submissionId)) {
      throw new IllegalArgumentException("Missing required param: submissionId");
    }

    photos = photoSubmissionDao.getAllPhotos(submissionId);
    json.put("total", photos.size());
    json.put("result", new JSONArray(util.toJson(photos)));

    return json;
  }

}
