package com.google.ytd.command;

public enum CommandType {
  GET_VIDEO_SUBMISSIONS(GetVideoSubmissions.class),
  UPDATE_VIDEO_SUBMISSION_STATUS(UpdateVideoSubmissionStatus.class),
  UPDATE_VIDEO_SUBMISSION_ADMIN_NOTES(UpdateVideoSubmissionAdminNotes.class),
  NEW_ASSIGNMENT(NewAssignment.class),
  NEW_MOBILE_VIDEO_SUBMISSION(NewMobileVideoSubmission.class),
  GET_ASSIGNMENTS(GetAssignments.class),
  UPDATE_ASSIGNMENT(UpdateAssignment.class),
  GET_ADMIN_CONFIG(GetAdminConfig.class),
  UPDATE_ADMIN_CONFIG(UpdateAdminConfig.class),
  GET_PHOTO_SUBMISSIONS(GetPhotoSubmissions.class),
  GET_ALL_PHOTO_ENTRIES(GetAllPhotoEntries.class),
  UPDATE_PHOTO_SUBMISSION_ADMIN_NOTES(UpdatePhotoSubmissionAdminNotes.class),
  UPDATE_PHOTO_ENTRIES_STATUS(UpdatePhotoEntriesStatus.class),
  DELETE_PHOTO_ENTRIES(DeletePhotoEntries.class),
  DELETE_PHOTO_SUBMISSION(DeletePhotoSubmission.class),
  VALIDATE_CAPTCHA(ValidateCaptcha.class),
  GET_VIDEO_DETAILS(GetVideoDetails.class),
  GET_YOUTUBE_CATEGORIES(GetYouTubeCategories.class),
  GET_YOUTUBE_CAPTIONS(GetYouTubeCaptions.class),
  GET_YOUTUBE_CAPTION_TRACK(GetYouTubeCaptionTrack.class),
  UPDATE_YOUTUBE_CAPTION_TRACK(UpdateYouTubeCaptionTrack.class),
  GET_YOUTUBE_VIDEOS(GetYouTubeVideos.class),
  DELETE_VIDEO_SUBMISSION(DeleteVideoSubmission.class),
  DELETE_STUFF(DeleteStuff.class),
  GET_YOUTUBE_PLAYLISTS(GetYouTubePlaylists.class),
  OPEN_CHANNEL_CONNECTION(OpenChannelConnection.class),
  GET_PICASA_ALBUMS(GetPicasaAlbums.class),
  UPDATE_VIDEO_SUBMISSION_ASSIGNMENT(UpdateVideoSubmissionAssignment.class);

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
