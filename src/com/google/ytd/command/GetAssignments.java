package com.google.ytd.command;

import java.util.List;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.inject.Inject;
import com.google.ytd.dao.AssignmentDao;
import com.google.ytd.model.Assignment;
import com.google.ytd.util.Util;

public class GetAssignments extends Command {
  private static final Logger LOG = Logger.getLogger(GetAssignments.class.getName());

  private AssignmentDao assignmentDao = null;

  @Inject
  private Util util;

  @Inject
  public GetAssignments(AssignmentDao assignmentDao) {
    this.assignmentDao = assignmentDao;
  }

  @Override
  public JSONObject execute() throws JSONException {
    LOG.info(this.toString());
    JSONObject json = new JSONObject();
    List<Assignment> assignments = null;

    // TODO(austinchau) Add params validation, preferably something more
    // structured and reusable
    // across all commands
    String sortBy = getParam("sortBy");
    String sortOrder = getParam("sortOrder");
    String filterType = getParam("filterType");
    int pageIndex = Integer.parseInt(getParam("pageIndex"));
    int pageSize = Integer.parseInt(getParam("pageSize"));

    assignments = assignmentDao.getAssignments(sortBy, sortOrder, filterType);
    int totalSize = assignments.size();
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

    assignments = assignments.subList(startIndex, endIndex);
    json.put("totalSize", totalSize);
    json.put("totalPages", totalPages);
    json.put("result", new JSONArray(util.toJson(assignments)));
    return json;
  }
}
