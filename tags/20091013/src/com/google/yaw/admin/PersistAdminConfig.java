/* Copyright (c) 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
 * Servlet responsible for saving updates to the AdminConfig singleton, based on JSON input.
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
    adminConfig.setDefaultTag(jsonObj.getDefaultTag());
    adminConfig.setLinkBackText(jsonObj.getLinkBackText());
    adminConfig.setModerationMode(jsonObj.getModerationMode());
    adminConfig.setBrandingMode(jsonObj.getBrandingMode());
    adminConfig.setSubmissionMode(jsonObj.getSubmissionMode());        
    adminConfig.setNewSubmissionAddress(jsonObj.getNewSubmissionAddress());
    adminConfig.setLoginInstruction(jsonObj.getLoginInstruction());
    adminConfig.setPostSubmitMessage(jsonObj.getPostSubmitMessage());
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