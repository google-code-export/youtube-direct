package com.google.ytd.util;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.appengine.api.mail.MailService;
import com.google.appengine.api.mail.MailServiceFactory;
import com.google.appengine.api.mail.MailService.Message;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.ytd.dao.AdminConfigDao;
import com.google.ytd.dao.PhotoSubmissionDao;
import com.google.ytd.model.AdminConfig;
import com.google.ytd.model.PhotoEntry;
import com.google.ytd.model.PhotoSubmission;
import com.google.ytd.model.VideoSubmission;
import com.google.ytd.model.VideoSubmission.ModerationStatus;

@Singleton
public class EmailUtil {
  private static final Logger log = Logger.getLogger(EmailUtil.class.getName());
  @Inject
  private AdminConfigDao adminConfigDao;

  @Inject
  private PhotoSubmissionDao photoSubmissionDao;

  @Inject
  private Util util;

  private void sendNewSubmissionEmail(String subject, String body) {
    try {
      AdminConfig adminConfig = adminConfigDao.getAdminConfig();

      String addressCommaSeparated = adminConfig.getNewSubmissionAddress();
      if (util.isNullOrEmpty(addressCommaSeparated)) {
        throw new IllegalArgumentException(
            "No notification email addresses found in configuration.");
      }
      String[] addresses = addressCommaSeparated.split("\\s*,\\s*");


      MailService mailService = MailServiceFactory.getMailService();
      Message message = new Message();

      // Default to the first (or only) address in the recipient list to use
      // as From: header.
      message.setSender(addresses[0]);
      message.setTo(addresses);
      message.setSubject(subject);
      message.setTextBody(body);

      mailService.send(message);
    } catch (IOException e) {
      log.log(Level.WARNING, "", e);
    } catch (IllegalArgumentException e) {
      log.log(Level.WARNING, "", e);
    }
  }

  public void sendNewSubmissionEmail(VideoSubmission videoSubmission) {
    String subject =
        String.format("New video submission for assignment id %d", videoSubmission
            .getAssignmentId());

    String body =
        String.format("Video %s was submitted by YouTube user %s in response to "
            + "assignment id %d.", videoSubmission.getWatchUrl(), videoSubmission.getYouTubeName(),
            videoSubmission.getAssignmentId());

    sendNewSubmissionEmail(subject, body);
  }

  public void sendNewSubmissionEmail(PhotoSubmission photoSubmission) {
    String subject =
        String.format("New photo submission for assignment id %d", photoSubmission
            .getAssignmentId());

    StringBuilder picasaUrls = new StringBuilder();
    for (PhotoEntry photoEntry : photoSubmissionDao.getAllPhotos(photoSubmission.getId())) {
      picasaUrls.append(photoEntry.getPicasaUrl());
      picasaUrls.append("\n");
    }

    String body =
        String.format("The following photos were submitted by %s (%s) in response to "
            + "assignment id %d.", photoSubmission.getAuthor(), photoSubmission.getNotifyEmail(),
            photoSubmission.getAssignmentId(), picasaUrls.toString());

    sendNewSubmissionEmail(subject, body);
  }

  private void sendUserModerationEmail(String toAddress, String subject, String body) {
    try {
      AdminConfig adminConfig = adminConfigDao.getAdminConfig();

      MailService mailService = MailServiceFactory.getMailService();
      Message message = new Message();

      String fromAddress = adminConfig.getFromAddress();
      if (util.isNullOrEmpty(fromAddress)) {
        throw new IllegalArgumentException("No from address found in configuration.");
      }

      message.setSender(fromAddress);
      message.setTo(toAddress);
      message.setSubject(subject);
      message.setTextBody(body);

      mailService.send(message);
    } catch (IOException e) {
      log.log(Level.WARNING, "", e);
    } catch (IllegalArgumentException e) {
      log.log(Level.WARNING, "", e);
    }
  }

  public void sendUserModerationEmail(VideoSubmission entry, ModerationStatus status) {
    try {
      AdminConfig adminConfig = adminConfigDao.getAdminConfig();

      String body;
      switch (status) {
        case APPROVED:
          body = adminConfig.getApprovalEmailText();
          break;

        case REJECTED:
          body = adminConfig.getRejectionEmailText();
          break;

        default:
          throw new IllegalArgumentException(String.format("ModerationStatus %s is not valid.",
              status.toString()));
      }
      if (util.isNullOrEmpty(body)) {
        throw new IllegalArgumentException("No email body found in configuration.");
      }

      body = body.replace("ARTICLE_URL", entry.getArticleUrl());
      body = body.replace("YOUTUBE_URL", entry.getWatchUrl());
      body = body.replace("MEDIA_URL", entry.getWatchUrl());

      String toAddress = entry.getNotifyEmail();
      if (util.isNullOrEmpty(toAddress)) {
        throw new IllegalArgumentException("No destination email address in VideoSubmission.");
      }

      sendUserModerationEmail(toAddress, "Your Recent Video Submission", body);
    } catch (IllegalArgumentException e) {
      log.log(Level.WARNING, "", e);
    }
  }

  public void sendUserModerationEmail(PhotoSubmission photoSubmission, PhotoEntry photoEntry,
      ModerationStatus status) {
    try {
      AdminConfig adminConfig = adminConfigDao.getAdminConfig();

      String body;
      switch (status) {
        case APPROVED:
          body = adminConfig.getApprovalEmailText();
          break;

        case REJECTED:
          body = adminConfig.getRejectionEmailText();
          break;

        default:
          throw new IllegalArgumentException(String.format("ModerationStatus %s is not valid.",
              status.toString()));
      }
      if (util.isNullOrEmpty(body)) {
        throw new IllegalArgumentException("No email body found in configuration.");
      }

      body = body.replace("ARTICLE_URL", photoSubmission.getArticleUrl());

      if (status == ModerationStatus.APPROVED) {
        body = body.replace("MEDIA_URL", photoEntry.getPicasaUrl());
      } else {
        body = body.replace("MEDIA_URL", "");
      }

      String toAddress = photoSubmission.getNotifyEmail();
      if (util.isNullOrEmpty(toAddress)) {
        throw new IllegalArgumentException("No destination email address in PhotoSubmission.");
      }

      sendUserModerationEmail(toAddress, "Your Recent Photo Submission", body);
    } catch (IllegalArgumentException e) {
      log.log(Level.WARNING, "", e);
    }
  }
}
