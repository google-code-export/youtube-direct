package com.google.ytd.command;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;

import com.google.appengine.api.utils.SystemProperty;
import com.google.appengine.repackaged.com.google.common.util.Base64;
import com.google.appengine.repackaged.com.google.common.util.Base64DecoderException;
import com.google.inject.Inject;
import com.google.ytd.dao.AdminConfigDao;
import com.google.ytd.model.AdminConfig;
import com.google.ytd.util.Util;

public class UpdateAdminConfig extends Command {
  private static final Logger LOG = Logger.getLogger(UpdateAdminConfig.class.getName());

  private AdminConfigDao adminConfigDao = null;

  @Inject
  private Util util;

  @Inject
  public UpdateAdminConfig(AdminConfigDao adminConfigDao) {
    this.adminConfigDao = adminConfigDao;
  }

  @Override
  public JSONObject execute() {
    JSONObject json = new JSONObject();
    String clientId = getParam("clientId");
    String developerKey = getParam("developerKey");
    String defaultTag = getParam("defaultTag");
    String linkBackText = getParam("linkBackText");
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
    String privateKeyBytes = getParam("privateKeyBytes");
    String maxPhotoSizeMb = getParam("maxPhotoSizeMb");
    String photoSubmissionEnabled = getParam("photoSubmissionEnabled");
    String recaptchaPrivateKey = getParam("recaptchaPrivateKey");
    String recaptchaPublicKey = getParam("recaptchaPublicKey");

    AdminConfig adminConfig = adminConfigDao.getAdminConfig();

    // Use the name of the App Engine isntance for the client id.
    adminConfig.setClientId(Util.CLIENT_ID_PREFIX + SystemProperty.applicationId.get());

    if (developerKey != null) {
      adminConfig.setDeveloperKey(developerKey);
    }

    if (defaultTag != null) {
      adminConfig.setDefaultTag(defaultTag);
    }

    if (linkBackText != null) {
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

    if (newSubmissionAddress != null) {
      adminConfig.setNewSubmissionAddress(newSubmissionAddress);
    }

    if (loginInstruction != null) {
      adminConfig.setLoginInstruction(loginInstruction);
    }

    if (postSubmitMessage != null) {
      adminConfig.setPostSubmitMessage(postSubmitMessage);
    }

    if (moderationEmail != null) {
      boolean isModerationEmailOn = moderationEmail.toLowerCase().equals("true");
      adminConfig.setModerationEmail(isModerationEmailOn);
    }

    if (fromAddress != null) {
      adminConfig.setFromAddress(fromAddress);
    }

    if (approvalEmailText != null) {
      adminConfig.setApprovalEmailText(approvalEmailText);
    }

    if (rejectionEmailText != null) {
      adminConfig.setRejectionEmailText(rejectionEmailText);
    }

    if (privateKeyBytes != null) {
      privateKeyBytes = privateKeyBytes.replace("-----BEGIN PRIVATE KEY-----", "");
      privateKeyBytes = privateKeyBytes.replace("-----END PRIVATE KEY-----", "");
      privateKeyBytes = privateKeyBytes.replace("\n", "");

      try {
        adminConfig.setPrivateKeyBytes(Base64.decode(privateKeyBytes));
      } catch (Base64DecoderException e) {
        LOG.log(Level.WARNING, "", e);
        adminConfig.setPrivateKeyBytes(new byte[0]);
      }
    }

    if (maxPhotoSizeMb != null) {
      adminConfig.setMaxPhotoSizeMb(Integer.parseInt(maxPhotoSizeMb));
    }

    if (photoSubmissionEnabled != null) {
      adminConfig.setPhotoSubmissionEnabled(photoSubmissionEnabled.equals("true"));
    }

    if (recaptchaPrivateKey != null) {
      adminConfig.setRecaptchaPrivateKey(recaptchaPrivateKey);
    }

    if (recaptchaPublicKey != null) {
      adminConfig.setRecaptchaPublicKey(recaptchaPublicKey);
    }

    adminConfig.setUpdated(new Date());
    adminConfig = adminConfigDao.save(adminConfig);

    return json;
  }
}
