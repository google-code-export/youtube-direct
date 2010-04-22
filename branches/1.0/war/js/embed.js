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

jQuery(document).ready( function() {
  init();
});

function init() {
  window.URL_PARAMS = getUrlParams();

  // Hide post submission message.
  jQuery('#postSubmitMessage').css('display', 'none');
  
  if (window.isLoggedIn) {
    highlightRequired();        
    
    // hide YouTube instruction
    jQuery('#loginInstruction').css('display', 'none');
    
    // show submissionAsk
    jQuery('#submissionAsk').css('display', 'block');
    
    jQuery('#uploadVideoButton').click( function(event) {
      jQuery('#submissionAsk').css('display', 'none');      
      uploaderMainInit();
    });

    jQuery('#existingVideoButton').click( function(event) {
      jQuery('#submissionAsk').css('display', 'none'); 
      existingVideoMainInit();
    });
    
  } else {
    // show YouTube instruction
    jQuery('#loginInstruction').css('display', 'block');
    
  }
}

function highlightRequired() {
  jQuery.each(jQuery('.required'), function(index, value) {
    jQuery(value).before('<span class="smallRed">*</span>&nbsp;')
  });  
}

function isRequiredFilled(sectionId) {    
  var ret = true;
  
  var elements = jQuery('#' + sectionId + ' .required');
  for (var i=0; i<elements.length; i++) {
    var e = jQuery(elements.get(i));
    var inputId = e.attr('for');    
    var input = jQuery('#' + inputId);
    var value = input.val();
    
    if (jQuery.trim(value) == '') {      
      ret = false;
      break;
    }
  }
  
  return ret;
}

function existingVideoMainInit() {
  jQuery('#existingVideoMain').css('display', 'block');
  
  jQuery('#submitButton').click( function(event) {
    if (!isRequiredFilled('existingVideoMain')) {
      event.preventDefault();
      showMessage('Please fill in all required field(s).');
      return;
    }
    
    var jsonObj = {};
    var url = jQuery('#videoUrl').val();
    jsonObj.videoId = getVideoId(url);
    if (jsonObj.videoId == null) {
      event.preventDefault();
      showMessage('Please enter a valid video URL.');
      return;
    }
    
    // disable buttons during submitting
    jQuery('#submitButton').get(0).disabled = true;
    jQuery('#cancelSubmitButton').get(0).disabled = true;    
    
    var location = jQuery('#submitLocation').val();  
    var date = jQuery('#submitDate').val();
    var email = jQuery('#submitEmail').val();

    jsonObj.location = location;
    jsonObj.date = date;
    jsonObj.email = email;
    
    var sessionId = window.URL_PARAMS.sessionId || '';
    
    var ajaxCall = {};
    ajaxCall.type = 'POST';
    ajaxCall.url = '/SubmitExistingVideo?sessionId=' + sessionId;
    ajaxCall.data = JSON.stringify(jsonObj);
    ajaxCall.dataType = 'json'; // expecting back
    ajaxCall.processData = false;
    ajaxCall.error = function(xhr, text, error) {
      clearProcessing();
      showMessage('Submit existing video incurred an error: ' + xhr.statusText);
      jQuery('#submitButton').get(0).disabled = false;
      jQuery('#cancelSubmitButton').get(0).disabled = false;         
    };
    ajaxCall.success = function(res) {
      clearProcessing();
      
      switch(res.success) {
        case 'true':
          jQuery('#existingVideoMain').css('display', 'none');
          // Show post submission message.
          jQuery('#postSubmitMessage').css('display', 'block');
          jQuery('#submitButton').get(0).disabled = false;
          jQuery('#cancelSubmitButton').get(0).disabled = false;             
          break;
        case 'false':         
          if (res.message) {
            showMessage(res.message);
          } else {          
            showMessage("Submit error incurred on server.")
          }      
          jQuery('#submitButton').get(0).disabled = false;
          jQuery('#cancelSubmitButton').get(0).disabled = false;              
      }
    };
    clearMessage();
    showProcessing('Submitting ...');
    jQuery.ajax(ajaxCall);    
  });

  jQuery('#cancelSubmitButton').click( function(event) {
    clearMessage();
    jQuery('#submissionAsk').css('display', 'block');
    jQuery('#existingVideoMain').css('display', 'none');
    jQuery('#uploaderMain').css('display', 'none');
  });      
  
  jQuery('#submitEmailAsk').change( function() {
    var checked = jQuery(this).get(0).checked;

    if (checked) {
      jQuery('#submitEmail').css('visibility', 'visible');
      jQuery('#submitEmail').focus();
    } else {
      jQuery('#submitEmail').css('visibility', 'hidden');
    }
  });

  jQuery("#submitDate").datepicker();  
}

function uploaderMainInit() {
  jQuery('#uploaderMain').css('display', 'block');

  jQuery('#uploadButton').click( function(event) {        
    if (!isRequiredFilled('uploaderMain')) {
      event.preventDefault();
      showMessage('Please fill in all required field(s).');
    } else {
      jQuery('#uploadButton').get(0).disabled = true;
      jQuery('#cancelUploadButton').get(0).disabled = true;
      getUploadToken();
      return false;
    }
  });

  jQuery('#cancelUploadButton').click( function(event) {
    clearMessage();
    jQuery('#submissionAsk').css('display', 'block');
    jQuery('#existingVideoMain').css('display', 'none');
    jQuery('#uploaderMain').css('display', 'none');
  });      
  
  jQuery('#uploadEmailAsk').change( function() {
    var checked = jQuery(this).get(0).checked;

    if (checked) {
      jQuery('#uploadEmail').css('visibility', 'visible');
      jQuery('#uploadEmail').focus();
    } else {
      jQuery('#uploadEmail').css('visibility', 'hidden');
    }
  });

  jQuery("#uploadDate").datepicker();
    
}

function getUploadToken() {
  var title = jQuery('#title').val();
  var description = jQuery('#description').val();
  var location = jQuery('#uploadLocation').val();  
  var date = jQuery('#uploadDate').val();
  var email = jQuery('#uploadEmail').val();
  var tagsString = jQuery('#tags').val();

  var tags = tagsString.split(',');
  jQuery.each(tags, function(index, value) {
    tags[index] = jQuery.trim(value);
  });

  var jsonObj = {};
  jsonObj.title = title;
  jsonObj.description = description;
  jsonObj.location = location;
  jsonObj.date = date;
  jsonObj.email = email;
  jsonObj.tags = tags;
  
  var sessionId = window.URL_PARAMS.sessionId || '';

  var ajaxCall = {};
  ajaxCall.type = 'POST';
  ajaxCall.url = '/GetUploadToken?sessionId=' + sessionId;
  ajaxCall.data = JSON.stringify(jsonObj);
  ajaxCall.dataType = 'json'; // expecting back
  ajaxCall.processData = false;
  ajaxCall.error = function(xhr, text, error) {
    clearProcessing(); 
    showMessage('Could not retrieve YouTube upload token: ' + xhr.statusText);
  };
  ajaxCall.success = function(res) {
    var uploadToken = res.uploadToken;
    var uploadUrl = res.uploadUrl;

    if (uploadToken == 'null' || uploadUrl == 'null') {
      // handle upload error
      jQuery('#uploading').empty();
      showMessage('Unexpected response from YouTube API: ' + res.error);
    } else {
      jQuery('#token').val(uploadToken);
      jQuery('#uploadForm').get(0).action = uploadUrl + '?nexturl='
          + getSelfUrl() + '/UploadResponseHandler?sessionId=' + sessionId;
      initiateUpload();
    }

  };
  clearMessage();
  showProcessing('Uploading ...');
  jQuery.ajax(ajaxCall);
}

function initiateUpload() {
  var iframeId = 'hiddenIframe';
  var hiddenIframe = jQuery('#' + iframeId).get(0);

  jQuery('#uploadForm').get(0).target = iframeId;

  var callback = function() {
    jQuery('#uploadButton').get(0).disabled = false;
    jQuery('#cancelUploadButton').get(0).disabled = false;
    jQuery('#postSubmitMessage').css('display', 'block');
    clearProcessing();
    jQuery('#uploaderMain').css('display', 'none');
  };

  // most browsers
  hiddenIframe.onload = callback;
  
  // IE 6 & 7
  hiddenIframe.onreadystatechange = function() {
    if (hiddenIframe.readyState == 'loaded'
        || hiddenIframe.readyState == 'complete') {
      if (self.frames[iframeId].name != iframeId) {
        /* IMPORTANT: This is a BUG FIX for Internet Explorer */
        self.frames[iframeId].name = iframeId;
      }
      callback();
    }
  };

  // submit the upload form!  
  jQuery('#uploadForm').get(0).submit();
}

function getUrlParams() {
  var args = new Object();
  var params = window.location.href.split('?');

  if (params.length > 1) {
    params = params[1];
    var pairs = params.split("&");
    for ( var i = 0; i < pairs.length; i++) {
      var pos = pairs[i].indexOf('=');
      if (pos == -1)
        continue;
      var argname = pairs[i].substring(0, pos);
      var value = pairs[i].substring(pos + 1);
      value = value.replace(/\+/g, " ");
      args[argname] = value;
    }
  }
  return args;
}

function getVideoId(url) {
  var matches = url.match(/\Wv=([^&]+)/i);
  if (matches && matches.length > 1) {
  	return matches[1];
  } else {
  	return null;
  }
}

function getSelfUrl() {
  var protocol = document.location.protocol;
  var host = document.location.host;
  return protocol + '//' + host;
}

function showMessage(text) {
  jQuery('#message').html(text);
}

function clearMessage(text) {
  jQuery('#message').empty();
}

function showProcessing(text) {
  var e = jQuery('#processing');
  e.html(text);
  e.css('display', 'block');
}

function clearProcessing() {
  var e = jQuery('#processing');
  e.empty();
  e.css('display', 'none');  
}