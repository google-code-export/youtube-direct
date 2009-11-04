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

function Yaw() {
    
}

Yaw.prototype.isAuthReturn = function() {
  return /#return$/i.test(document.location.href);
};

Yaw.prototype.setAssignmentId = function(id) {
	this.assignmentId = id;
};

Yaw.prototype.setArticleUrl = function(url) {
	this.articleUrl = url;
};

Yaw.prototype.setYawContainer = function(id, width, height) {
  
  var defaultHeight = 550;
  var defaultWidth = 350;
  
  this.yawContainer = id;
  this.width = width || defaultWidth;
  this.height = height || defaultHeight;
};

Yaw.prototype.setCallToAction = function(id) {
  this.callToAction = id;
  
  var callToAction = document.getElementById(this.callToAction);
  
  var self = this;
  
  callToAction.onclick = function() {    
    callToAction.style.display = 'none';
    self.embed();       
    return false;
  };  
};

Yaw.prototype.ready = function() {
  if (/#return$/i.test(document.location.href)) {
    var callToAction = document.getElementById(this.callToAction);
    callToAction.style.display = 'none';
    this.embed();
  }  
};

Yaw.prototype.embed = function() {
	var iframeElement = document.createElement('iframe');
	iframeElement.width = this.width + 'px';
	iframeElement.height = this.height + 'px';
	iframeElement.style.border = '0px solid gray';
	iframeElement.scrolling = 'no';
	iframeElement.frameBorder = '0';

	this.articleUrl = this.articleUrl || document.location.href;
	// remove hash link
	this.articleUrl = this.articleUrl.replace(/#.+$/, '');	
	
	var iframeUrl = 'http://' + getScriptSelfDomain() + '/embed?articleUrl=' + escape(this.articleUrl)
	    + '&assignmentId=' + this.assignmentId + '&width=' + this.width + '&height=' + this.height;
	iframeElement.src = iframeUrl;
	
	var iframeContainer = document.getElementById(this.yawContainer);
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

