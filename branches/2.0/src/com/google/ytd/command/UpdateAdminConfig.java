package com.google.ytd.command;

import java.util.Date;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.inject.Inject;
import com.google.ytd.dao.AdminConfigDao;
import com.google.ytd.model.AdminConfig;
import com.google.ytd.util.Util;

public class UpdateAdminConfig extends Command {
  private AdminConfigDao adminConfigDao = null;

  private static final Logger LOG = Logger.getLogger(UpdateAdminConfig.class.getName());

  @Inject
  private Util util;

  @Inject
  public UpdateAdminConfig(AdminConfigDao adminConfigDao) {
    this.adminConfigDao = adminConfigDao;
  }

  @Override
  public JSONObject execute() throws JSONException {
    LOG.info(this.toString());
    JSONObject json = new JSONObject();
    String clientId = getParam("clientId");
    String developerKey = getParam("developerKey");
    String defaultTag = getParam("defaultTag");
    String linkBackText = getParam("inkBackText");
    String moderationMode = getParam("moderationMode");
    String brandingMode = getParam("brandingMode");
    String submissionMode = getParam("submissionMode");
    String newSubmissionAddress = getParam("newSubmissionAddress");
    String loginInstruction = getParam("loginInstruction");
    String postSubmitMessage = getParam("postSubmitMessage");
    String moderationEmail = getParam("moderationEmail");
    String fromAddress = getParam("fromAddress");
    String approvalEmailText = getParam("approvalEmailText");
    String rejectionEmailText = getParam("rejectionEmailText");

    AdminConfig adminConfig = adminConfigDao.getAdminConfig();

    if (!util.isNullOrEmpty(clientId)) {
      adminConfig.setClientId(clientId);
    }

    if (!util.isNullOrEmpty(developerKey)) {
      adminConfig.setDeveloperKey(developerKey);
    }

    if (!util.isNullOrEmpty(defaultTag)) {
      adminConfig.setDefaultTag(defaultTag);
    }

    if (!util.isNullOrEmpty(linkBackText)) {
      adminConfig.setLinkBackText(linkBackText);
    }

    if (!util.isNullOrEmpty(moderationMode)) {
      adminConfig.setModerationMode(Integer.parseInt(moderationMode));
    }

    if (!util.isNullOrEmpty(brandingMode)) {
      adminConfig.setBrandingMode(Integer.parseInt(brandingMode));
    }

    if (!util.isNullOrEmpty(submissionMode)) {
      adminConfig.setSubmissionMode(Integer.parseInt(submissionMode));
    }

    if (!util.isNullOrEmpty(newSubmissionAddress)) {
      adminConfig.setNewSubmissionAddress(newSubmissionAddress);
    }

    if (!util.isNullOrEmpty(loginInstruction)) {
      adminConfig.setLoginInstruction(loginInstruction);
    }

    if (!util.isNullOrEmpty(postSubmitMessage)) {
      adminConfig.setPostSubmitMessage(postSubmitMessage);
    }

    if (!util.isNullOrEmpty(moderationEmail)) {
      boolean isModerationEmailOn = moderationEmail.toLowerCase().equals("true");
      adminConfig.setModerationEmail(isModerationEmailOn);
    }

    if (!util.isNullOrEmpty(fromAddress)) {
      adminConfig.setFromAddress(fromAddress);
    }

    if (!util.isNullOrEmpty(approvalEmailText)) {
      adminConfig.setApprovalEmailText(approvalEmailText);
    }

    if (!util.isNullOrEmpty(rejectionEmailText)) {
      adminConfig.setRejectionEmailText(rejectionEmailText);
    }

    adminConfig.setUpdated(new Date());
    adminConfig = adminConfigDao.save(adminConfig);

    return json;
  }
}
