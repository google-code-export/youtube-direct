package com.google.ytd.command;

public enum CommandType {
	NEW_SUBMISSION(NewSubmission.class), GET_SUBMISSIONS(GetSubmissions.class),
	UPDATE_SUBMISSION_STATUS(UpdateSubmissionStatus.class), UPDATE_SUBMISSION_ADMIN_NOTES(
			UpdateSubmissionAdminNotes.class), NEW_ASSIGNMENT(NewAssignment.class), GET_ASSIGNMENTS(
			GetAssignments.class), UPDATE_ASSIGNMENT(UpdateAssignment.class), GET_ADMIN_CONFIG(
			GetAdminConfig.class), UPDATE_ADMIN_CONFIG(UpdateAdminConfig.class);

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
