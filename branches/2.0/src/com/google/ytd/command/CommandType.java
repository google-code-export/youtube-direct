package com.google.ytd.command;

public enum CommandType {
  NEW_SUBMISSION(NewSubmissionCommand.class),
  GET_SUBMISSIONS(GetSubmissionsCommand.class),
  SET_SUBMISSION_STATUS(SetSubmissionStatusCommand.class),
  NEW_ASSIGNMENT(NewAssignmentCommand.class);

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
