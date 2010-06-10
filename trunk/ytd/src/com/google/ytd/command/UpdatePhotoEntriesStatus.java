package com.google.ytd.command;

import org.json.JSONObject;

import com.google.inject.Inject;
import com.google.ytd.dao.AssignmentDao;
import com.google.ytd.dao.PhotoSubmissionDao;
import com.google.ytd.model.Assignment;
import com.google.ytd.model.PhotoEntry;
import com.google.ytd.model.PhotoSubmission;
import com.google.ytd.model.PhotoEntry.ModerationStatus;
import com.google.ytd.picasa.PicasaApiHelper;
import com.google.ytd.util.Util;

public class UpdatePhotoEntriesStatus extends Command {

  private PhotoSubmissionDao photoSubmissionDao = null;
  private AssignmentDao assignmentDao = null;
  private PicasaApiHelper picasaApi = null;
  private Util util = null;

  @Inject
  public UpdatePhotoEntriesStatus(PhotoSubmissionDao submissionDao, Util util,
      AssignmentDao assignmentDao, PicasaApiHelper picasaApi) {
    this.photoSubmissionDao = submissionDao;
    this.util = util;
    this.assignmentDao = assignmentDao;
    this.picasaApi = picasaApi;
  }

  @Override
  public JSONObject execute() {
    JSONObject json = new JSONObject();
    String ids = getParam("ids");
    String status = getParam("status");

    if (util.isNullOrEmpty(ids)) {
      throw new IllegalArgumentException("Missing required param: ids");
    }
    if (util.isNullOrEmpty(status)) {
      throw new IllegalArgumentException("Missing required param: status");
    }
    status = status.toUpperCase();

    // This code is much less efficient because we can't count on all the ids in
    // the list being part of the same PhotoSubmission. I guess we could cache
    // here in the future.
    for (String id : ids.split(",")) {
      PhotoEntry entry = photoSubmissionDao.getPhotoEntry(id);
      PhotoSubmission submission = photoSubmissionDao.getSubmissionById(entry.getSubmissionId());
      Assignment assignment = assignmentDao.getAssignmentById(submission.getAssignmentId());

      if (entry.getBlobKey() != null || util.isNullOrEmpty(entry.getPicasaUrl())) {
        throw new IllegalStateException(
            String
                .format(
                    "Can't update the state of PhotoEntry id '%s' because it has not yet been moved from App Engine to Picasa.",
                    entry.getId()));
      }

      String newAlbumUrl;
      ModerationStatus statusEnum = ModerationStatus.valueOf(status);
      if (statusEnum == ModerationStatus.APPROVED) {
        newAlbumUrl = assignment.getPublicAlbumUrl();
      } else {
        newAlbumUrl = assignment.getPrivateAlbumUrl();
      }

      String newPhotoUrl = picasaApi.moveToNewAlbum(entry.getPicasaUrl(), newAlbumUrl);
      if (newPhotoUrl == null) {
        throw new IllegalStateException(String.format(
            "Couldn't move Picasa photo '%s' to album '%s'. Check AppEngine log for details.",
            entry.getPicasaUrl(), newAlbumUrl));
      }

      entry.setStatus(statusEnum);
      entry.setPicasaUrl(newPhotoUrl);
      
      photoSubmissionDao.save(entry);
    }

    return json;
  }
}
