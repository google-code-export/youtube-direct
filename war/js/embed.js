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
    
    jQuery('#photoButton').click(function(event) {
    	jQuery('#submissionAsk').css('display', 'none');
    	photoMainInit();
    });
    
    addFileElement();
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
  var elements = jQuery('#' + sectionId + ' .required');
  for (var i = 0; i < elements.length; i++) {
    var e = jQuery(elements.get(i));
    var inputId = e.attr('for');
    var input = jQuery('#' + sectionId).find('#' + inputId);
    var value = input.val();
    
    if (jQuery.trim(value) == '') {  
      return false;
    }
  }
  
  return true;
}

function processFileElements(section) {
	var emptyFileElements = [];
	var fileElements = section.find('input[type="file"]');
	
	for (var i = 0; i < fileElements.length; i++) {
		var fileElement = jQuery(fileElements.get(i));
		
		if (fileElement.val() == '') {
			emptyFileElements.push(fileElement);
		}
	}
	
	if (emptyFileElements.length < fileElements.length) {
		for (var i = 0; i < emptyFileElements.length; i++) {
			var emptyFileElement = jQuery(emptyFileElements[i]);
			emptyFileElement.unbind();
			jQuery('#wrapper-' + emptyFileElement.get(0).id).remove();
		}
		
		return true;
	} else {
		return false;
	}
}

function validateCaptcha() {
	var photoMain = jQuery('#photoMain');
	
  // Disable buttons during submission.
  photoMain.find('#submitButton').disabled = true;
  photoMain.find('#cancelSubmitButton').disabled = true;    
  
  var params = {};
  params.challenge = jQuery('#recaptcha_challenge_field').val();
  params.response = jQuery('#recaptcha_response_field').val();

  jsonrpc.makeRequest('VALIDATE_CAPTCHA', params, function(data) {
  	var json = JSON.parse(data);
  	
  	if (json.result == 'true') {
  		startPhotoUpload();
    } else {
    	photoMain.find('#submitButton').disabled = false;
    	photoMain.find('#cancelSubmitButton').disabled = false;
    	
    	Recaptcha.reload();
    	
    	if (json.result == 'false') {
    		showMessage('Incorrect word verification. Please try again.');
    	} else {
    		showMessage('An error occurred while contacting the word verification server. ' +
    						'Please try again later.');
    	}
    }
  });
}

function startPhotoUpload() {
	var photoMain = jQuery('#photoMain');
  
  showProcessing('Uploading photo...');

  initiateUpload(jQuery('#photoUploadForm'));
}

function photoMainInit() {
	var photoMain = jQuery('#photoMain');

	photoMain.css('display', 'block');
	
  photoMain.find('#uploadButton').click(function(event) {
  	clearMessage();
  	
    if (!isRequiredFilled('photoMain')) {
      showMessage('Please fill in all required field(s).');
      return false;
    }
    
    if (!processFileElements(jQuery('#photoMain'))) {
      showMessage('Please select at least one file to upload.');
      return false;
    }
    
    validateCaptcha();

    return false;
  });

  photoMain.find('#cancelUploadButton').click(function(event) {
    clearMessage();
    jQuery('#submissionAsk').css('display', 'block');
    jQuery('#photoMain').css('display', 'none');
  });      
  
  photoMain.find("#submitDate").datepicker();
}

function addFileElement() {
	var photoUploadForm = jQuery('#photoUploadForm');
	var siblingElement = photoUploadForm.find('#assignmentId');
	
	var fileElementCount = photoUploadForm.find('input[type="file"]').length;
	
	if (fileElementCount < 5) {
		var fileElementId = 'file' + fileElementCount;
	
		siblingElement.before('<div class="upload" id="wrapper-' + fileElementId + '"><label for="' +
						fileElementId + '">Select File: </label><input type="file" name="' + fileElementId +
						'" id="' + fileElementId + '" onChange="addFileElement()" /></div>');
	}					
}

function existingVideoMainInit() {
  
  jQuery('#existingVideoMain').css('display', 'block');
  
  jQuery('#submitButton').click( function(event) {

    if (!isRequiredFilled('existingVideoMain')) {
      event.preventDefault();
      showMessage('Please fill in all required field(s).');
      return;
    }        
    
    // disable buttons during submitting
    jQuery('#submitButton').get(0).disabled = true;
    jQuery('#cancelSubmitButton').get(0).disabled = true;    
    
    var url = jQuery('#videoUrl').val();   
    var location = jQuery('#submitLocation').val();  
    var date = jQuery('#submitDate').val();
    var email = jQuery('#submitEmail').val();

    var jsonObj = {};
    jsonObj.videoId = getVideoId(url);
    jsonObj.location = location;
    jsonObj.date = date;
    jsonObj.email = email;  
    
    var ajaxCall = {};
    ajaxCall.type = 'POST';
    ajaxCall.url = '/SubmitExistingVideo';
    ajaxCall.data = JSON.stringify(jsonObj);
    ajaxCall.dataType = 'json'; // expecting back
    ajaxCall.processData = false;
    ajaxCall.error = function(xhr, text, error) {
      clearProcessing();
      showMessage('Submit existing video incurred an error: ' + xhr.statusText);
    };
    ajaxCall.success = function(res) {
      clearProcessing();
      
      switch(res.success) {
        case 'true':
          jQuery('#existingVideoMain').css('display', 'none');
          // Show post submission message.
          jQuery('#postSubmitMessage').css('display', 'block');            
          break;
        case 'false':
          if (res.message) {
            showMessage(res.message);
          } else {          
            showMessage("Submit error incurred on server.")
          }  
      }
      jQuery('#submitButton').get(0).disabled = false;
      jQuery('#cancelSubmitButton').get(0).disabled = false;       
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

  var ajaxCall = {};
  ajaxCall.type = 'POST';
  ajaxCall.url = '/GetUploadToken';
  ajaxCall.data = JSON.stringify(jsonObj);
  ajaxCall.dataType = 'json'; // expecting back
  ajaxCall.processData = false;
  ajaxCall.error = function(xhr, text, error) {
    clearProcessing(); 
    showMessage('Could not retrieve YouTube upload token: ' + xhr.statusText);
    jQuery('#uploadButton').get(0).disabled = false;
    jQuery('#cancelUploadButton').get(0).disabled = false;      
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
          + getSelfUrl() + '/UploadResponseHandler';
      initiateUpload(jQuery('#uploadForm'));
    }
  };
  clearMessage();
  showProcessing('Uploading ...');
  jQuery.ajax(ajaxCall);
}

function initiateUpload(uploadForm) {
  var iframeName = 'hiddenIframe';
  var iframeId = iframeName;

  var hiddenIframe = document.createElement('iframe');
  hiddenIframe.src = '/blank.html';
  hiddenIframe.style.display = 'none';
  hiddenIframe.id = iframeId;
  hiddenIframe.name = iframeName;

  uploadForm.attr('target', iframeName);

  var callback = function() {
  	uploadForm.find('#uploadButton').disabled = false;
  	uploadForm.find('#cancelUploadButton').disabled = false;
  	jQuery('#postSubmitMessage').css('display', 'block');
    clearProcessing();
    jQuery('#uploaderMain').css('display', 'none');
    jQuery('#photoMain').css('display', 'none');

    // if I care about the iframe content
    /*
     * var rawResponse = window.frames[0].document.body.innerHTML; rawResponse =
     * rawResponse.replace('<pre>', ''); rawResponse = rawResponse.replace('</pre>',
     * ''); console.log(rawResponse);
     * 
     * var json = JSON.parse(rawResponse); var youTubeId = json.id; var status =
     * json.status;
     * 
     * if (status == '200') { jQuery('#message').html('upload completed!');
     * jQuery('#uploaderMain').empty(); } else { jQuery('#message').html('upload
     * failed, please try again!'); }
     */
  };

  // most browsers
  hiddenIframe.onload = callback;

  // IE 6 & 7
  hiddenIframe.onreadystatechange = function() {
    if (hiddenIframe.readyState == 'loaded'
        || hiddenIframe.readyState == 'complete') {

      if (self.frames[iframeId].name != iframeId) {
        /* *** IMPORTANT: This is a BUG FIX for Internet Explorer *** */
        self.frames[iframeId].name = iframeId;
      }
      callback();
    }
  };

  jQuery(document.body).append(hiddenIframe);

  // submit the upload form!  
  uploadForm.submit();
}

function getVideoId(url) {
  return url.replace('http://www.youtube.com/watch?v=', '');
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