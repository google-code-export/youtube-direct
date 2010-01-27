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

// namespace protection against collision
var admin = admin || {};
admin.config = admin.config || {};

admin.config.init = function() {
  var saveButton = jQuery('#saveButton');     
  
  admin.config.getAdminConfig(function(data) {
    jQuery('#developerKey').val(data.developerKey);
    jQuery('#clientId').val(data.clientId);
    jQuery('#defaultTag').val(data.defaultTag);
    jQuery('#linkBackText').val(data.linkBackText);
    jQuery('#moderationMode').val(data.moderationMode);
    jQuery('#newSubmissionAddress').val(data.newSubmissionAddress);
    jQuery('#brandingMode').val(data.brandingMode);
    jQuery('#loginInstruction').val(unescape(data.loginInstruction));
    jQuery('#postSubmitMessage').val(unescape(data.postSubmitMessage));
    jQuery('#fromAddress').val(data.fromAddress);
    jQuery('#approvalEmailText').val(unescape(data.approvalEmailText));
    jQuery('#rejectionEmailText').val(unescape(data.rejectionEmailText));
    
    if (data.moderationEmail) {
      jQuery('#moderationEmail').attr('checked', true);
      admin.config.toggleModerationEmailTextDiv(true);
    } else {
      jQuery('#moderationEmail').attr('checked', false);
      admin.config.toggleModerationEmailTextDiv(false);
    }
    
    if (data.youTubeAuthSubToken && data.youTubeUsername) {
      jQuery('#youTubeUsername').html("Authenticated as <a href='http://youtube.com/" + data.youTubeUsername + "'>" + data.youTubeUsername + "</a>");
      jQuery('#authenticateButton').val("Re-Authenticate");
    }
  });
  
  saveButton.click(function() {
    admin.config.updateAdminConfig();
  });
  
  jQuery('#authenticateButton').click(function() {
    // Hardcode http:// rather than allowing for https:// to ensure that we can get by with
    // registering http://APP.appspot.com/ as the prefix for AuthSub requests in the Google
    // Manage Your Domain pages.
    var nextUrl = "http://" + window.location.host + "/admin/PersistAuthSubToken";
    window.location = "https://www.google.com/accounts/AuthSubRequest?next=" + nextUrl + "&scope=http%3A%2F%2Fgdata.youtube.com&session=1&secure=0";
  });
  
  jQuery('#moderationEmail').click(function() {
    admin.config.toggleModerationEmailTextDiv();
  });
};

admin.config.toggleModerationEmailTextDiv = function(isVisible) {
  if (isVisible == null) {
    isVisible = jQuery('#moderationEmailTextDiv').css("display") == "none" ? true : false;
  }
  
  var displayStyle = isVisible ? 'inline' : 'none';
  
  jQuery('#moderationEmailTextDiv').css("display", displayStyle);
}

admin.config.getAdminConfig = function(callback) {
  var messageElement = admin.showMessage('Loading configuration...');
  
  var command = 'GET_ADMIN_CONFIG';
  var params = {};
  
  var jsonRpcCallback = function(jsonStr) {
    try {
      var json = JSON.parse(jsonStr);
      if (!json.error) {
        admin.showMessage('Configuration loaded.', messageElement);
        callback(json.result);
      } else {
        admin.showError(json.error, messageElement);
      }
    } catch(exception) {
      admin.showError(jsonStr, messageElement);
    }
  } 
  
  jsonrpc.makeRequest(command, params, jsonRpcCallback);  
};

admin.config.updateAdminConfig = function() {
  var messageElement = admin.showMessage("Saving configuration...");
  
  var developerKey = jQuery('#developerKey').val();   
  var clientId = jQuery('#clientId').val();
  var defaultTag = jQuery('#defaultTag').val();
  var linkBackText = jQuery('#linkBackText').val();
  var moderationMode = jQuery('#moderationMode').val();
  var newSubmissionAddress = jQuery('#newSubmissionAddress').val();
  var brandingMode = jQuery('#brandingMode').val();
  var submissionMode = jQuery('#submissionMode').val();
  var loginInstruction = escape(jQuery('#loginInstruction').val());
  var postSubmitMessage = escape(jQuery('#postSubmitMessage').val());
  var moderationEmail = jQuery('#moderationEmail').attr('checked');
  var fromAddress = jQuery('#fromAddress').val();
  var approvalEmailText = escape(jQuery('#approvalEmailText').val());
  var rejectionEmailText = escape(jQuery('#rejectionEmailText').val());
  
  var params = {};
  params.developerKey = developerKey;
  params.clientId = clientId;
  params.defaultTag = defaultTag;
  params.linkBackText = linkBackText;
  params.moderationMode = moderationMode;
  params.newSubmissionAddress = newSubmissionAddress;
  params.brandingMode = brandingMode;  
  params.submissionMode = submissionMode;  
  params.loginInstruction = loginInstruction;
  params.postSubmitMessage = postSubmitMessage;
  params.moderationEmail = moderationEmail;
  params.fromAddress = fromAddress;
  params.approvalEmailText = approvalEmailText;
  params.rejectionEmailText = rejectionEmailText;

  var command = 'UPDATE_ADMIN_CONFIG';
  
  var jsonRpcCallback = function(jsonStr) {
    try {
      var json = JSON.parse(jsonStr);
      if (!json.error) {
        admin.showMessage("Configuration saved.", messageElement);
      } else {
        admin.showError(json.error, messageElement);
      }
    } catch(exception) {
      admin.showError(jsonStr, messageElement);
    }
  } 
  
  jsonrpc.makeRequest(command, params, jsonRpcCallback);
};
