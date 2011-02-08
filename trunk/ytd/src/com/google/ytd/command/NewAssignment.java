package com.google.ytd.command;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.inject.Inject;
import com.google.ytd.dao.AssignmentDao;
import com.google.ytd.model.Assignment;
import com.google.ytd.model.Assignment.AssignmentStatus;
import com.google.ytd.util.Util;

public class NewAssignment extends Command {

  private AssignmentDao assignmentDao = null;

  @Inject
  private Util util;

  @Inject
  public NewAssignment(AssignmentDao assignmentDao) {
    this.assignmentDao = assignmentDao;
  }

  @Override
  public JSONObject execute() throws JSONException {
    JSONObject json = new JSONObject();
    String status = getParam("status");
    String description = getParam("description");
    String category = getParam("category");
    String title = getParam("title");
    String loginInstruction = getParam("loginInstruction");
    String postSubmitMessage = getParam("postSubmitMessage");

    if (util.isNullOrEmpty(description)) {
      throw new IllegalArgumentException("Missing required param: description");
    }

    if (util.isNullOrEmpty(status)) {
      throw new IllegalArgumentException("Missing required param: status");
    }

    if (util.isNullOrEmpty(category)) {
      throw new IllegalArgumentException("Missing required param: category");
    }

    if (util.isNullOrEmpty(title)) {
      throw new IllegalArgumentException("Missing required param: title");
    }

    Assignment assignment = new Assignment();
    assignment.setStatus(AssignmentStatus.valueOf(status.toUpperCase()));
    assignment.setDescription(description);
    assignment.setCategory(category);
    assignment.setLoginInstruction(loginInstruction);
    assignment.setPostSubmitMessage(postSubmitMessage);
    assignment.setTitle(title);

    assignment = assignmentDao.newAssignment(assignment, title);

    json.put("id", assignment.getId());

    return json;
  }
}
