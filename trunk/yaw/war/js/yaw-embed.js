function Yaw() {}

Yaw.prototype.setAssignmentId = function(id) {
	this.assignmentId = id;
};

Yaw.prototype.setArticleUrl = function(url) {
	this.articleUrl = url;
};

Yaw.prototype.embed = function(containerId, width, height) {
	var iframeElement = document.createElement('iframe');
	iframeElement.width = width + 'px';
	iframeElement.height = height + 'px';
	iframeElement.style.border = '0px solid gray';

	this.articleUrl = this.articleUrl || document.location.href;

	var iframeUrl = 'http://' + getScriptSelfDomain() + '/embed?articleUrl=' + this.articleUrl
	    + '&assignmentId=' + this.assignmentId;

	iframeElement.src = iframeUrl;
	//console.log(iframeUrl);
	
	var iframeContainer = document.getElementById(containerId);
	iframeContainer.innerHTML = '';
	iframeContainer.appendChild(iframeElement);
};

function getSelfUrl() {
	var protocol = document.location.protocol;
	var host = document.location.host;
	return protocol + '//' + host;
}

function getScriptSelfDomain() {
  var scriptDomain = null;
  var scripts = document.getElementsByTagName('script');
  
  for (var i=0; i<scripts.length; i++) {
    var script = scripts[i];
    var scriptUrl = script.getAttribute('src');
    
    // regex to detect if this is the embed.js script tag    
    if (isEmbedScript(scriptUrl)) {
      if (isRelativePath(scriptUrl)) {
        scriptDomain = document.location.host;
      } else {
        var re = /https?:\/\/([-\w\.]+)+(:\d+)?(\/([\w/_\.]*(\?\S+)?)?)?/;
        if (re.test(scriptUrl)) {
          scriptDomain = RegExp.$1;
          var port = RegExp.$2;
          if (port && port.length > 0) {
            scriptDomain += port
          }
        }        
      }
      break;
    }           
  }
  return scriptDomain;
}

function isEmbedScript(url) {
  var isEmbedScript = false;
  var re = /.+yaw-embed.js$/;  
  
  if (re.test(url)) {
    isEmbedScript = true;
  }
  return isEmbedScript;    
}

function isRelativePath(url) {
  var isRelative = false;
  var re = /^http.+/;  
  
  if (!re.test(url)) {
    isRelative = true;
  }
  return isRelative;
}

