package com.google.ytd.command;

import java.util.Date;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.inject.Inject;
import com.google.ytd.dao.AdminConfigDao;
import com.google.ytd.dao.SubmissionDao;
import com.google.ytd.model.AdminConfig;
import com.google.ytd.model.VideoSubmission;
import com.google.ytd.model.AdminConfig.BrandingModeType;
import com.google.ytd.model.VideoSubmission.ModerationStatus;
import com.google.ytd.util.EmailUtil;
import com.google.ytd.util.Util;

public class SetSubmissionStatusCommand extends Command {
  private static final Logger LOG = Logger.getLogger(SetSubmissionStatusCommand.class.getName());
  private SubmissionDao submissionDao = null;
  private AdminConfigDao adminConfigDao = null;

  @Inject
  private Util util;

  @Inject
  private EmailUtil emailUtil;

  @Inject
  public SetSubmissionStatusCommand(SubmissionDao submissionDao, AdminConfigDao adminConfigDao) {
    this.submissionDao = submissionDao;
    this.adminConfigDao = adminConfigDao;
  }

  @Override
  public JSONObject execute() throws JSONException {
    LOG.info(this.toString());
    JSONObject json = new JSONObject();
    String id = getParam("id");
    String status = getParam("status");

    if (util.isNullOrEmpty(id)) {
      throw new IllegalArgumentException("Missing required param: id");
    }
    if (util.isNullOrEmpty(id)) {
      throw new IllegalArgumentException("Missing required param: id");
    }

    VideoSubmission submission = submissionDao.getSubmission(id);
    ModerationStatus newStatus = ModerationStatus.valueOf(status.toUpperCase());
    ModerationStatus currentStatus = submission.getStatus();

    if (newStatus == currentStatus) {
      return json;
    } else {
      switch(newStatus) {
        case APPROVED:
          onApproved(submission);
          break;
        case REJECTED:
          break;
        case SPAM:
          break;
        case UNREVIEWED:
          break;
      }
    }

    return json;
  }

  private void onApproved(VideoSubmission submission) {

    AdminConfig adminConfig = adminConfigDao.getAdminConfig();

    submission.setStatus(ModerationStatus.APPROVED);
    submission.setUpdated(new Date());
    submissionDao.save(submission);

    // Notify the submitter of approval if applicable
    if (adminConfig.isModerationEmail() && (submission.getNotifyEmail() != null)) {
      emailUtil.sendNotificationEmail(submission, ModerationStatus.APPROVED);
    }

    // Turn branding on if applicable
    if (adminConfigDao.getAdminConfig().getBrandingMode() == BrandingModeType.ON.ordinal()) {
      String linkBackText = adminConfig.getLinkBackText();
      if (!util.isNullOrEmpty(linkBackText)) {
        String prependText = linkBackText.replace("ARTICLE_URL", submission.getArticleUrl());

        if (!submission.getVideoDescription().contains(prependText)) {
          // We only want to update the video if the text isn't already there.
          //updateVideoDescription(submission, prependText, adminConfig.getDefaultTag());
        }
      }
    }

  }

}
