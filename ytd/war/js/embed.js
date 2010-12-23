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

jQuery(document).ready(function() {
  init();
});

function init() {
	window.URL_PARAMS = getUrlParams();

	highlightRequired();        

	jQuery('#uploadVideoButton').click( function(event) {
		jQuery('#loginInstruction').css('display', 'none');      
		uploaderMainInit();
	});

	jQuery('#existingVideoButton').click( function(event) {
		jQuery('#loginInstruction').css('display', 'none'); 
		existingVideoMainInit();
	});

	jQuery('#photoButton').click(function(event) {
		jQuery('#loginInstruction').css('display', 'none');
		photoMainInit();
	});
	
	if (navigator.geolocation) {
	  jQuery.each(jQuery('input[name="location"]'), function(index, locationField) {
	    locationField.style.width = "90%";
	    jQuery('<img src="/images/geolocation_icon.jpg" title="Auto-detect your current location." style="margin-left: 5px; cursor: pointer;">').insertAfter(locationField).click(function(event) {
	      navigator.geolocation.getCurrentPosition(function(position) {
	        jQuery('#latitude').val(position.coords.latitude);
	        jQuery('#longitude').val(position.coords.longitude);
	        
	        var geocoder = new google.maps.Geocoder();
	        var latlng = new google.maps.LatLng(position.coords.latitude, position.coords.longitude);
	        
	        geocoder.geocode({'latLng': latlng}, function(results, status) {
	          if (status == google.maps.GeocoderStatus.OK && results[1]) {
	            locationField.value = results[1].formatted_address;
	          } else {
	            locationField.value = position.coords.latitude + ', ' + position.coords.longitude;
	          }
	        });
	      });  
	    });
	  });
	}

	addFileElement();
}

function highlightRequired() {
  jQuery.each(jQuery('.required'), function(index, value) {
    jQuery(value).after('&nbsp;<span class="smallRed">(required)</span>&nbsp;')
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

function validateTags(section) {
	var tags = section.find('#tags').val();
	
	if (tags.length >= 490) {
		return false;
	}
	
	var splits = tags.split(',');
	for (var i = 0; i < splits.length; i++) {
		if (splits[i].length >= 29) {
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
  photoMain.find('#uploadButton').get(0).disabled = true;
  photoMain.find('#cancelUploadButton').get(0).disabled = true;    
  
  var params = {};
  params.challenge = jQuery('#recaptcha_challenge_field').val();
  params.response = jQuery('#recaptcha_response_field').val();

  jsonrpc.makeRequest('VALIDATE_CAPTCHA', params, function(data) {
  	var json = JSON.parse(data);
  	
  	if (json.result == 'true') {
  		startPhotoUpload();
    } else {
    	photoMain.find('#uploadButton').get(0).disabled = false;
    	photoMain.find('#cancelUploadButton').get(0).disabled = false;
    	
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
    
  	if (!isValidEmail(photoMain.find('#uploadEmail').val())) {
      showMessage('Your email address appears to be invalid.');
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
    photoMain.css('display', 'none');
    jQuery('#loginInstruction').css('display', 'block');
  });      
  
  jQuery("#photoDate").datepicker();
}

function addFileElement() {
	var photoUploadForm = jQuery('#photoUploadForm');
	var siblingElement = photoUploadForm.find('#articleUrl');
	
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
  
  var params = {};
  params.username = jQuery('#youTubeName').text();

  jsonrpc.makeRequest('GET_YOUTUBE_VIDEOS', params, function(json) {
  	jQuery('#loadingVideos').attr('style', 'display: none;');
  	
    try {
      if (!json.error) {
      	displayExistingVideos(json.videos);
      } else {
        showMessage(json.error);
      }
    } catch(exception) {
    	showMessage('Request failed: ' + exception);
    }
  });
  
  jQuery('#submitButton').click( function(event) {
    if (!isRequiredFilled('existingVideoMain')) {
      event.preventDefault();
      showMessage('Please fill in all required field(s).');
      return;
    }
    
  	if (jQuery('#submitEmailAsk').get(0).checked && !isValidEmail(jQuery('#submitEmail').val())) {
  	  event.preventDefault();
      showMessage('Your email address appears to be invalid.');
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

    // Disable buttons while submitting.
    jQuery('#submitButton').get(0).disabled = true;
    jQuery('#cancelSubmitButton').get(0).disabled = true;

    var location = jQuery('#submitLocation').val();  
    var phoneNumber = jQuery('#phoneNumber').val();  
    var date = jQuery('#submitDate').val();
    var email = jQuery('#submitEmail').val();
    
    var assignmentIdField = jQuery('#existingVideoMain').find('#assignmentId');

    jsonObj.location = location;
    jsonObj.phoneNumber = phoneNumber;
    jsonObj.date = date;
    jsonObj.email = email;
    
    if (assignmentIdField != null) {
      jsonObj.assignmentId = assignmentIdField.val();
    }
    
    var sessionId = window.URL_PARAMS.sessionId || '';
    
    var ajaxCall = {};
    ajaxCall.type = 'POST';
    ajaxCall.url = '/SubmitExistingVideo?sessionId=' + sessionId;
    ajaxCall.data = JSON.stringify(jsonObj);
    ajaxCall.dataType = 'json'; // expecting back
    ajaxCall.processData = false;
    ajaxCall.error = function(xhr, text, error) {
      clearProcessing();
      
      var errorText;
      if (xhr.responseText) {
        try {
          errorText = JSON.parse(xhr.responseText).error;
        } catch (exception) {
          errorText = xhr.responseText;
        }
      } else {
      	errorText = xhr.statusText;
      }
      
      showMessage('Sorry, your video submission failed: ' + errorText);
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
            showMessage("Sorry, your video submission failed.")
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
    jQuery('#existingVideoMain').css('display', 'none');
    jQuery('#loginInstruction').css('display', 'block');
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

function displayExistingVideos(videos) {
	var options = [];
	for (var videoId in videos) {
		options.push('<option value="' + videoId + '">' + videos[videoId].title + '</option>');
	}
	
	var videosSelect = jQuery('#videosSelect');
	videosSelect.append(options.join('\n'));
	
	var videoUrlInput = jQuery('#videoUrl');
	var thumbnailImg = jQuery('#thumbnail');
	var descriptionDiv = jQuery('#existingVideoDescription');
	
	videosSelect.change(function() {
		var selectedVideoId = videosSelect.val();

		if (videos[selectedVideoId]) {
			descriptionDiv.html(videos[selectedVideoId].description);
			videoUrlInput.val(videos[selectedVideoId].videoUrl);
			
			if (videos[selectedVideoId].thumbnailUrl) {
				thumbnailImg.attr('src', videos[selectedVideoId].thumbnailUrl);
				thumbnailImg.attr('style', 'float: left; display: block; margin: 5px;');
			} else {
				thumbnailImg.attr('style', 'display: none');
			}
		} else {
			descriptionDiv.html('');
			videoUrlInput.val('');
			thumbnailImg.attr('style', 'display: none');
		}
	});
	
	jQuery('#existingVideos').attr('style', 'display: block; margin-top: 7px;');
}

function uploaderMainInit() {
  jQuery('#uploaderMain').css('display', 'block');

  jQuery('#uploadButton').click(function(event) {        
    if (!isRequiredFilled('uploaderMain')) {
      event.preventDefault();
      showMessage('Please fill in all required field(s).');
      return false;
    }
    
    if (!validateTags(jQuery('#uploaderMain'))) {
        event.preventDefault();
        showMessage('Please use fewer, or shorter, tag values.');
        return false;
    }
    
  	if (jQuery('#uploadEmailAsk').get(0).checked &&
  					!isValidEmail(jQuery('#uploaderMain').find('#uploadEmail').val())) {
  		event.preventDefault();
      showMessage('Your email address appears to be invalid.');
      return false;
  	}
    
    jQuery('#uploadButton').get(0).disabled = true;
    jQuery('#cancelUploadButton').get(0).disabled = true;
    getUploadToken();
    return false;
  });

  jQuery('#cancelUploadButton').click(function(event) {
    clearMessage();
    jQuery('#uploaderMain').css('display', 'none');
    jQuery('#loginInstruction').css('display', 'block');
  });      
  
  jQuery('#uploadEmailAsk').change(function() {
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
  var phoneNumber = jQuery('#uploadPhoneNumber').val(); 
  var date = jQuery('#uploadDate').val();
  var email = jQuery('#uploadEmail').val();
  var tagsString = jQuery('#tags').val();
  
  var assignmentIdField = jQuery('#uploaderMain').find('#assignmentId');

  var tags = tagsString.split(',');
  jQuery.each(tags, function(index, value) {
    tags[index] = jQuery.trim(value);
  });

  var jsonObj = {};
  jsonObj.title = title;
  jsonObj.description = description;
  jsonObj.location = location;
  jsonObj.phoneNumber = phoneNumber;
  jsonObj.date = date;
  jsonObj.email = email;
  jsonObj.tags = tags;
  
  if (jQuery('#latitude').val()) {
    jsonObj.latitude = jQuery('#latitude').val();
  }
  if (jQuery('#longitude').val()) {
    jsonObj.longitude = jQuery('#longitude').val();
  }
  
  if (assignmentIdField != null) {
    jsonObj.assignmentId = assignmentIdField.val();
  }
  
  var sessionId = window.URL_PARAMS.sessionId || '';

  var ajaxCall = {};
  ajaxCall.type = 'POST';
  ajaxCall.url = '/GetUploadToken?sessionId=' + sessionId;
  ajaxCall.data = JSON.stringify(jsonObj);
  ajaxCall.dataType = 'json'; // expecting back
  ajaxCall.processData = false;
  ajaxCall.error = function(xhr, text, error) {
    clearProcessing();
    
    var errorText;
    if (xhr.responseText) {
      try {
        errorText = JSON.parse(xhr.responseText).error;
      } catch (exception) {
        errorText = xhr.responseText;
      }
    } else {
    	errorText = xhr.statusText;
    }
    
    showMessage('Could not retrieve YouTube upload token: ' + errorText);
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
          + getSelfUrl() + '/UploadResponseHandler?sessionId=' + sessionId;
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

  var hiddenIframe;
  try {
  	hiddenIframe = document.createElement('<iframe name="' + iframeName + '">');
  } catch (e) {
  	hiddenIframe = document.createElement('iframe');
    hiddenIframe.name = iframeName;
  }
  hiddenIframe.src = '/blank.html';
  hiddenIframe.style.display = 'none';
  hiddenIframe.id = iframeId;

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

// Hopefully-good-enough regex for checking to see if an email address is "valid".
function isValidEmail(email) {
	var regex = /[a-z0-9!#$%&'*+\/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+\/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?/;

	return regex.test(email);
}