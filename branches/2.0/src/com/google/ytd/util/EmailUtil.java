package com.google.ytd.util;

import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.ytd.dao.AdminConfigDao;
import com.google.ytd.model.AdminConfig;
import com.google.ytd.model.VideoSubmission;
import com.google.ytd.model.VideoSubmission.ModerationStatus;

@Singleton
public class EmailUtil {
	private static final Logger log = Logger.getLogger(EmailUtil.class.getName());
	@Inject
	private AdminConfigDao adminConfigDao;

	@Inject
	private Util util;

	public void sendNewSubmissionEmail(VideoSubmission videoSubmission) {
		AdminConfig adminConfig = adminConfigDao.getAdminConfig();

		String address = adminConfig.getNewSubmissionAddress();
		if (!util.isNullOrEmpty(address)) {
			try {
				Properties props = new Properties();
				Session session = Session.getDefaultInstance(props, null);
				Message msg = new MimeMessage(session);

				msg.setFrom(new InternetAddress(address, address));
				msg.addRecipient(Message.RecipientType.TO, new InternetAddress(address, address));

				msg.setSubject(String.format("New submission for assignment id %d", videoSubmission
						.getAssignmentId()));

				msg.setText(String.format("Video %s was submitted by YouTube user %s in response to "
						+ "assignment id %d.", videoSubmission.getWatchUrl(), videoSubmission.getYouTubeName(),
						videoSubmission.getAssignmentId()));

				Transport.send(msg);
			} catch (UnsupportedEncodingException e) {
				log.log(Level.WARNING, "", e);
			} catch (MessagingException e) {
				log.log(Level.WARNING, "", e);
			}
		}
	}

	public void sendNotificationEmail(VideoSubmission entry, ModerationStatus status) {
		try {
			String toAddress = entry.getNotifyEmail();
			if (util.isNullOrEmpty(toAddress)) {
				throw new IllegalArgumentException("No destination email address in VideoSubmission.");
			}

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

			String fromAddress = adminConfig.getFromAddress();
			if (util.isNullOrEmpty(fromAddress)) {
				throw new IllegalArgumentException("No from address found in configuration.");
			}

			body = body.replace("ARTICLE_URL", entry.getArticleUrl());
			body = body.replace("YOUTUBE_URL", entry.getWatchUrl());

			Properties props = new Properties();
			Session session = Session.getDefaultInstance(props, null);
			Message msg = new MimeMessage(session);

			msg.setFrom(new InternetAddress(fromAddress, fromAddress));
			msg.addRecipient(Message.RecipientType.TO, new InternetAddress(toAddress, toAddress));

			msg.setSubject("Your Recent Video Submission");

			msg.setText(body);

			Transport.send(msg);

			log.info(String.format("Sent %s notification email for status %s", toAddress, status
					.toString()));
		} catch (IllegalArgumentException e) {
			log.log(Level.WARNING, "", e);
		} catch (UnsupportedEncodingException e) {
			log.log(Level.WARNING, "", e);
		} catch (MessagingException e) {
			log.log(Level.WARNING, "", e);
		}
	}
}
