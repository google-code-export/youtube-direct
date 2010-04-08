package com.google.ytd.command;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.inject.Inject;
import com.google.ytd.dao.AssignmentDao;
import com.google.ytd.model.Assignment;
import com.google.ytd.util.Util;

public class GetAssignments extends Command {
  private AssignmentDao assignmentDao = null;

  @Inject
  private Util util;

  @Inject
  public GetAssignments(AssignmentDao assignmentDao) {
    this.assignmentDao = assignmentDao;
  }

  @Override
  public JSONObject execute() throws JSONException {
    JSONObject json = new JSONObject();
    List<Assignment> assignments = null;

    String sortBy = getParam("sortBy");
    String sortOrder = getParam("sortOrder");
    String filterType = getParam("filterType");
    String pageIndexString = getParam("pageIndex");
    String pageSizeString = getParam("pageSize");

    if (util.isNullOrEmpty(sortBy)) {
      throw new IllegalArgumentException("Missing required param: sortBy");
    }
    if (util.isNullOrEmpty(sortOrder)) {
      throw new IllegalArgumentException("Missing required param: sortOrder");
    }
    if (util.isNullOrEmpty(filterType)) {
      throw new IllegalArgumentException("Missing required param: filterType");
    }
    if (util.isNullOrEmpty(pageIndexString)) {
      throw new IllegalArgumentException("Missing required param: pageIndex");
    }
    if (util.isNullOrEmpty(pageSizeString)) {
      throw new IllegalArgumentException("Missing required param: pageSize");
    }

    int pageIndex = Integer.parseInt(pageIndexString);
    int pageSize = Integer.parseInt(pageSizeString);

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
