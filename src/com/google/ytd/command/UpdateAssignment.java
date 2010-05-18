package com.google.ytd.command;

import org.json.JSONObject;

import com.google.inject.Inject;
import com.google.ytd.dao.AssignmentDao;
import com.google.ytd.model.Assignment;
import com.google.ytd.model.Assignment.AssignmentStatus;
import com.google.ytd.util.Util;

public class UpdateAssignment extends Command {
  private AssignmentDao assignmentDao = null;

  @Inject
  private Util util;

  @Inject
  public UpdateAssignment(AssignmentDao assignmentDao) {
    this.assignmentDao = assignmentDao;
  }

  @Override
  public JSONObject execute() {
    JSONObject json = new JSONObject();

    String id = getParam("id");

    if (util.isNullOrEmpty(id)) {
      throw new IllegalArgumentException("Missing required param: id");
    }

    Assignment assignment = assignmentDao.getAssignmentById(id);

    String status = getParam("status");
    String description = getParam("description");
    String category = getParam("category");

    if (!util.isNullOrEmpty(status)) {
      assignment.setStatus(AssignmentStatus.valueOf(status.toUpperCase()));
    }
    if (!util.isNullOrEmpty(description)) {
      assignment.setDescription(description);
    }

    if (!util.isNullOrEmpty(category)) {
      assignment.setCategory(category);
    }

    assignment = assignmentDao.save(assignment);

    return json;
  }
}
