// namespace protection against collision
var admin = admin || {};
admin.config = admin.config || {};

admin.config.init = function() {
  var saveButton = jQuery('#saveButton');     
  
  admin.config.getAdminConfig(function(data) {
    jQuery('#developerKey').val(data.developerKey);
    jQuery('#clientId').val(data.clientId);
    jQuery('#youTubeUsername').val(data.youTubeUsername);
    jQuery('#youTubePassword').val(data.youTubePassword);
    jQuery('#defaultTag').val(data.defaultTag);
    jQuery('#moderationMode').val(data.moderationMode);
    jQuery('#brandingMode').val(data.brandingMode);
    jQuery('#brandingMode').val(data.brandingMode);
    jQuery('#loginInstruction').val(unescape(data.loginInstruction));  
  });
  
  saveButton.click(function() {
    admin.config.persistAdminConfig();
  });
};

admin.config.getAdminConfig = function(callback) {
  var ajaxCall = {};
  ajaxCall.type = 'GET';
  ajaxCall.url = '/GetAdminConfig';
  ajaxCall.dataType = 'json'; // expecting back
  ajaxCall.processData = false;
  ajaxCall.error = function(xhr, text, error) {
    console.log('Get admin config incurred an error: ' + xhr.statusText);
  };
  ajaxCall.success = function(res) {
    admin.config.clearConfigureStatus();
    callback(res);
  };
  admin.config.showConfigureStatus("loading ...");
  jQuery.ajax(ajaxCall);     
};

admin.config.persistAdminConfig = function() {
  var developerKey = jQuery('#developerKey').val();   
  var clientId = jQuery('#clientId').val();
  var youTubeUsername = jQuery('#youTubeUsername').val();
  var youTubePassword = jQuery('#youTubePassword').val();
  var defaultTag = jQuery('#defaultTag').val();
  var moderationMode = jQuery('#moderationMode').val();
  var brandingMode = jQuery('#brandingMode').val();
  var submissionMode = jQuery('#submissionMode').val();
  var loginInstruction = escape(jQuery('#loginInstruction').val());
  
  var jsonObj = {};
  jsonObj.developerKey = developerKey;
  jsonObj.clientId = clientId;
  jsonObj.youTubeUsername = youTubeUsername;
  jsonObj.youTubePassword = youTubePassword;
  jsonObj.defaultTag = defaultTag;
  jsonObj.moderationMode = moderationMode;
  jsonObj.brandingMode = brandingMode;  
  jsonObj.submissionMode = submissionMode;  
  jsonObj.loginInstruction = loginInstruction;
  
  var ajaxCall = {};
  ajaxCall.type = 'POST';
  ajaxCall.url = '/PersistAdminConfig';
  ajaxCall.data = JSON.stringify(jsonObj);
  ajaxCall.dataType = 'json'; // expecting back
  ajaxCall.processData = false;
  ajaxCall.error = function(xhr, text, error) {
    admin.config.showConfigureStatus('Persist admin config incurred an error: ' + xhr.statusText);
  };
  ajaxCall.success = function(res) {
    admin.config.showConfigureStatus('saved!');
  };
  admin.config.showConfigureStatus('saving ...');
  jQuery.ajax(ajaxCall);    
};

admin.config.showConfigureStatus = function(text) {
  var status = jQuery('#configureStatus');
  status.html(text);
};

admin.config.clearConfigureStatus = function() {
  jQuery('#configureStatus').empty();
};