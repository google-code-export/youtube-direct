jQuery(document).ready(function() {
	init();	
});

function init() {
	window.URL_PARAMS = getUrlParams();
	
	jQuery('#uploadButton').click(function() {		
		var img = jQuery('<img/>');
		img.attr('src', 'loading.gif');
		jQuery('#uploading').append(img);		
		getUploadToken();			
		return false;
	});	
	
	jQuery('#emailCheckbox').change(function() {
		var checked = jQuery(this).get(0).checked;
		
		if (checked) {
			jQuery('#email').css('visibility', 'visible');			
		} else {
			jQuery('#email').css('visibility', 'hidden');			
		}
	});
	
	jQuery("#date").datepicker();

}

function getUploadToken() {
	var title = jQuery('#title').val();
	var description = jQuery('#description').val();
	var location = jQuery('#location').val();
	var email = jQuery('#email').val();
	
	var tagsString = jQuery('#tags').val();
	
	var tags = tagsString.split(',');	
	jQuery.each(tags, function(index, value) {
		tags[index] = jQuery.trim(value);
	});
	
	var jsonObj = {};
	jsonObj.title = title;
	jsonObj.description = description;
	jsonObj.location = location;
	jsonObj.email = email;
	jsonObj.tags = tags;

	console.log(jsonObj);
	
    var ajaxCall = {};
    ajaxCall.type = 'POST';
    ajaxCall.url = '/GetUploadToken';
    ajaxCall.data = JSON.stringify(jsonObj);
    ajaxCall.dataType = 'json'; //expecting back
    ajaxCall.processData = false;
    ajaxCall.success = function(res) {        
        var uploadToken = res.uploadToken;
        var uploadUrl = res.uploadUrl;
        
		console.log(uploadToken);
		console.log(uploadUrl);
        
		if (uploadToken == 'null' || uploadUrl == 'null') {
			// handle upload error
			jQuery('#uploading').empty();
			jQuery('#message').html(res.error);
			
		} else {		
	        jQuery('#token').val(uploadToken);        
	        jQuery('#uploadForm').get(0).action = uploadUrl + 
	        	'?nexturl=' + getSelfUrl() + '/UploadResponseHandler';
	
	        initiateUpload();
		}
        
    };
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
  		
    	jQuery('#message').html('Upload completed!');
    	jQuery('#uploaderMain').empty();
  		
  		// if I care about the iframe content
  		/*
	    var rawResponse = window.frames[0].document.body.innerHTML;
	    rawResponse = rawResponse.replace('<pre>', '');
	    rawResponse = rawResponse.replace('</pre>', '');
		console.log(rawResponse);

		var json = JSON.parse(rawResponse);
		var youTubeId = json.id;
		var status = json.status;
		
	    if (status == '200') {
	    	jQuery('#message').html('upload completed!');
	    	jQuery('#uploaderMain').empty();
	    } else {
	    	jQuery('#message').html('upload failed, please try again!');
	    }
	    */ 
	};

	// most browsers
	hiddenIframe.onload = callback;  
	
	// IE 6 & 7
	hiddenIframe.onreadystatechange = function() {
	  if (hiddenIframe.readyState == 'loaded' || hiddenIframe.readyState == 'complete') {
	
	    if(self.frames[iframeId].name != iframeId) { 
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
	    if ( pos == -1 ) continue;
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