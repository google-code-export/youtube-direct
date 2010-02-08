package com.google.ytd.command;

import java.util.List;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.inject.Inject;
import com.google.ytd.dao.PhotoSubmissionDao;
import com.google.ytd.model.PhotoSubmission;
import com.google.ytd.util.Util;

public class GetPhotoSubmissions extends Command {
  private static final Logger LOG = Logger.getLogger(GetPhotoSubmissions.class.getName());

  private PhotoSubmissionDao photoSubmissionDao = null;

  @Inject
  private Util util;

  @Inject
  public GetPhotoSubmissions(PhotoSubmissionDao photoSubmissionDao) {
    this.photoSubmissionDao = photoSubmissionDao;
  }

  @Override
  public JSONObject execute() throws JSONException {
    LOG.info(this.toString());
    JSONObject json = new JSONObject();
    List<PhotoSubmission> submissions = null;

    String sortBy = getParam("sortBy");
    String sortOrder = getParam("sortOrder");
    String pageIndexString = getParam("pageIndex");
    String pageSizeString = getParam("pageSize");

    if (util.isNullOrEmpty(sortBy)) {
      throw new IllegalArgumentException("Missing required param: sortBy");
    }
    if (util.isNullOrEmpty(sortOrder)) {
      throw new IllegalArgumentException("Missing required param: sortOrder");
    }
    if (util.isNullOrEmpty(pageIndexString)) {
      throw new IllegalArgumentException("Missing required param: pageIndex");
    }
    if (util.isNullOrEmpty(pageSizeString)) {
      throw new IllegalArgumentException("Missing required param: pageSize");
    }

    int pageIndex = Integer.parseInt(pageIndexString);
    int pageSize = Integer.parseInt(pageSizeString);

    submissions = photoSubmissionDao.getPhotoSubmissions(sortBy, sortOrder);

    int totalSize = submissions.size();
    int totalPages = (int) Math.ceil(((double) totalSize / (double) pageSize));
    int startIndex = (pageIndex - 1) * pageSize; // inclusive
    int endIndex = -1; // exclusive

    if (pageIndex < totalPages) {
      endIndex = startIndex + pageSize;
    } else {
      if (pageIndex == totalPages && totalSize % pageSize == 0) {
        endIndex = startIndex + pageSize;
      } else {
        endIndex = startIndex + (totalSize % pageSize);
      }
    }

    submissions = submissions.subList(startIndex, endIndex);
    json.put("totalSize", totalSize);
    json.put("totalPages", totalPages);

    json.put("result", new JSONArray(util.toJson(submissions)));

    return json;
  }

}
