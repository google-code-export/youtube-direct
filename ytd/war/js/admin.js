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

jQuery(document).ready(function() {
	if (window.isLoggedIn) {
		jQuery('#tabs').tabs();
		admin.init();
	}
});

admin.init = function() {
  admin.sub.init(); // from video_submission.js	
  admin.photo.init(); // from photo_submission.js   
  admin.assign.init(); // from assignments.js
  admin.config.init(); //from configuration.js
};

admin.showMessage = function(message, elementToHide, displaySeconds) {
  // Default timeout is 5 sec.
  displaySeconds = displaySeconds || 5;
  return admin.showSomething(message, 'message', elementToHide, displaySeconds);
};

// errorObj may be a XHR object returned from an AJAX call, or a string.
admin.showError = function(errorString, elementToHide) {
  return admin.showSomething(errorString, 'error', elementToHide, 10);
};

admin.showSomething = function(message, elementClass, elementToHide, displaySeconds) {
  var wrapperElement = jQuery('<p>').addClass('messageListWrapper').prependTo('#messageList');
  var messageElement = jQuery('<span>' + message + '</span>').addClass(elementClass);
  messageElement.prependTo(wrapperElement);
  
  if (elementToHide) {
    jQuery(elementToHide).hide();
  }
  
  if (typeof displaySeconds == 'number') {
    setTimeout(function() {
      messageElement.fadeOut('fast');
    }, displaySeconds * 1000);
  }
  
  return wrapperElement;
};

admin.formatDate = function(date) {
  var year = admin.padZero(date.getFullYear());
  var month = admin.padZero(date.getMonth() + 1);
  var day = admin.padZero(date.getDate());
  var hours = admin.padZero(date.getHours());
  var minutes = admin.padZero(date.getMinutes());
  var seconds = admin.padZero(date.getSeconds());
  
  // Use %s to maintain zero-padding, which the jQuery.sprintf() library can't provide.
  return jQuery.sprintf('%s-%s-%s %s:%s:%s', year, month, day, hours, minutes, seconds);
};

admin.padZero = function(value) {
  value = value + '';
  if (value.length < 2) {
    return '0' + value;
  } else {
    return value;
  }
};