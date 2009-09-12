jQuery(document).ready( function() {
  init();
});

function init() {
  if (window.isLoggedIn) {

    jQuery(document.body).css('background', 'lightGray');

    window.URL_PARAMS = getUrlParams();

    jQuery('#newUploadLink').click( function(event) {
      event.preventDefault();
      jQuery('#submissionAsk').css('display', 'none');
      uploaderMainInit();
    });

    jQuery('#existingVideoLink').click( function(event) {
      event.preventDefault();
      jQuery('#submissionAsk').css('display', 'none');
      existingVideoMainInit();
    });
    
  }
}

function existingVideoMainInit() {
  jQuery('#existingVideoMain').css('display', 'block');
  
  jQuery('#submitButton').click( function() {
    var videoId = jQuery('#videoId').val();   
    var location = jQuery('#submitLocation').val();  
    var date = jQuery('#submitDate').val();
    var email = jQuery('#submitEmail').val();

    var jsonObj = {};
    jsonObj.videoId = videoId;
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
      clearSubmitRunning();
      showMessage('Submit existing video incurred an error: ' + xhr.statusText);
    };
    ajaxCall.success = function(res) {
      clearSubmitRunning();
      
      switch(res.success) {
        case 'true':
          jQuery('#existingVideoMain').css('display', 'none');
          showMessage("thank you!");   
          break;
        case 'false':
          if (res.message) {
            showMessage(res.message);
          } else {          
            showMessage("Submit error incurred on server.")
          }      
      }
    };
    showSubmitRunning();
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

  jQuery('#uploadButton').click( function() {
    getUploadToken();
    return false;
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
    clearUploadRunning();    
    showMessage('Could not retrieve YouTube upload token: ' + xhr.statusText);
  };
  ajaxCall.success = function(res) {
    //console.log(res);
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

      initiateUpload();
    }

  };
  showUploadRunning();
  jQuery.ajax(ajaxCall);
}

function initiateUpload() {

  var iframeName = 'hiddenIframe';
  var iframeId = iframeName;

  var hiddenIframe = document.createElement('iframe');
  hiddenIframe.src = '/blank.html';
  hiddenIframe.style.display = 'none';
  hiddenIframe.id = iframeId;
  hiddenIframe.name = iframeName;

  jQuery('#uploadForm').get(0).target = iframeName;

  var callback = function() {
    showMessage('Upload completed!');
    clearSubmitRunning();
    jQuery('#uploaderMain').css('display', 'none');

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

function showUploadRunning() {
  var img = jQuery('<img/>');
  img.attr('src', 'loading.gif');
  jQuery('#uploadRunning').append(img);    
}

function clearUploadRunning() {
  jQuery('#uploadRunning').empty();  
}

function showSubmitRunning() {
  var img = jQuery('<img/>');
  img.attr('src', 'loading.gif');
  jQuery('#submitRunning').append(img);    
}

function clearSubmitRunning() {
  jQuery('#submitRunning').empty();  
}