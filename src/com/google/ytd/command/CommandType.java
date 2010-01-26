package com.google.ytd.command;

public enum CommandType {
  GET_VIDEO_SUBMISSIONS(
      GetVideoSubmissions.class),
  UPDATE_VIDEO_SUBMISSION_STATUS(
      UpdateVideoSubmissionStatus.class),
  UPDATE_VIDEO_SUBMISSION_ADMIN_NOTES(
      UpdateVideoSubmissionAdminNotes.class),
  NEW_ASSIGNMENT(
      NewAssignment.class),
  GET_ASSIGNMENTS(
      GetAssignments.class),
  UPDATE_ASSIGNMENT(
      UpdateAssignment.class),
  GET_ADMIN_CONFIG(
      GetAdminConfig.class),
  UPDATE_ADMIN_CONFIG(
      UpdateAdminConfig.class);

  private Class<? extends Command> clazz = null;

  CommandType(Class<? extends Command> clazz) {
    this.clazz = clazz;
  }

  public Class<? extends Command> getClazz() {
    return clazz;
  }

  public static CommandType valueOfIngoreCase(String name) {
    return CommandType.valueOf(name.toUpperCase());
  }
}
