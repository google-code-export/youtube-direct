package com.google.yaw.admin;

import com.google.yaw.Util;
import com.google.yaw.model.AdminConfig;

import java.io.IOException;
import java.util.Date;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * JSON write endpoint for AdminConfig model from admin ajax UI
 */
public class PersistAdminConfig extends HttpServlet {
  private static final Logger log = Logger.getLogger(PersistAdminConfig.class.getName());

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    PersistenceManagerFactory pmf = Util.getPersistenceManagerFactory();
    PersistenceManager pm = pmf.getPersistenceManager();
    
    String json = Util.getPostBody(req); 
    
    AdminConfig jsonObj = Util.GSON.fromJson(json, AdminConfig.class);
            
    AdminConfig adminConfig = Util.getAdminConfig();
    
    adminConfig.setClientId(jsonObj.getClientId());
    adminConfig.setDeveloperKey(jsonObj.getDeveloperKey());
    adminConfig.setYouTubeUsername(jsonObj.getYouTubeUsername());
    adminConfig.setYouTubePassword(jsonObj.getYouTubePassword());
    adminConfig.setDefaultTag(jsonObj.getDefaultTag());
    adminConfig.setLinkBackText(jsonObj.getLinkBackText());
    adminConfig.setModerationMode(jsonObj.getModerationMode());
    adminConfig.setBrandingMode(jsonObj.getBrandingMode());
    adminConfig.setSubmissionMode(jsonObj.getSubmissionMode());        
    adminConfig.setNewSubmissionAddress(jsonObj.getNewSubmissionAddress());
    adminConfig.setLoginInstruction(jsonObj.getLoginInstruction());
    adminConfig.setModerationEmail(jsonObj.isModerationEmail());
    adminConfig.setFromAddress(jsonObj.getFromAddress());
    adminConfig.setApprovalEmailText(jsonObj.getApprovalEmailText());
    adminConfig.setRejectionEmailText(jsonObj.getRejectionEmailText());
    adminConfig.setUpdated(new Date());
    
    pm.makePersistent(adminConfig);
    pm.close();
    
    resp.setContentType("text/javascript");
    resp.getWriter().println(Util.GSON.toJson(adminConfig));
  }
}