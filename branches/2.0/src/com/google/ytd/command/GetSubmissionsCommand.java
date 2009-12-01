package com.google.ytd.command;

import java.util.List;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.inject.Inject;
import com.google.ytd.Util;
import com.google.ytd.dao.SubmissionManager;
import com.google.ytd.model.VideoSubmission;

public class GetSubmissionsCommand extends Command {
  private static final Logger LOG = Logger.getLogger(GetSubmissionsCommand.class.getName());

  private SubmissionManager submissionManager = null;

  @Inject
  public GetSubmissionsCommand(SubmissionManager submissionManager) {
    this.submissionManager = submissionManager;
  }

  @Override
  public JSONObject execute() throws JSONException {
    LOG.info(this.toString());
    JSONObject json = new JSONObject();
    List<VideoSubmission> submissions = null;

    // TODO(austinchau) Add params validation, preferably something more structured and reusable
    // across all commands
    String sortBy = getParam("sortBy");
    String sortOrder = getParam("sortOrder");
    String filterType = getParam("filterType");
    int pageIndex = Integer.parseInt(getParam("pageIndex"));
    int pageSize = Integer.parseInt(getParam("pageSize"));

    submissions = submissionManager.getSubmissions(sortBy, sortOrder, filterType);
    int totalSize = submissions.size();
    int totalPages = (int) Math.ceil(((double)totalSize/(double)pageSize));
    int startIndex = (pageIndex - 1) * pageSize; //inclusive
    int endIndex = -1; //exclusive

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
    json.put("totalPages", totalPages);
    json.put("result", Util.GSON.toJson(submissions));
    return json;
  }
}
