//TODO this will be changed when the script is pushed to a production location
var YAW_EMBED_URL = getSelfUrl() + '/embed';

function Yaw() {
	
}

Yaw.prototype.setArticleId = function(id) {
	this.articleId = id;
};	

Yaw.prototype.setArticleUrl = function(url) {
	this.articleUrl = url;
};	

Yaw.prototype.setPartnerId = function(id) {
	this.partnerId = id;
};	

Yaw.prototype.embed = function(containerId, width, height) {
    var iframeElement = document.createElement('iframe');
    iframeElement.width = width + 'px';
    iframeElement.height = height + 'px';
    iframeElement.style.border = '1px solid gray';
    
    this.articleUrl = this.articleUrl || document.location.href;
    
	var iframeUrl = YAW_EMBED_URL + '?articleUrl=' 
		+ this.articleUrl + '&articleId=' + this.articleId 
		+ '&partnerId=' + this.partnerId;
    
    iframeElement.src = iframeUrl;
    
    var iframeContainer = document.getElementById(containerId);
    iframeContainer.innerHTML = '';
    iframeContainer.appendChild(iframeElement);		
};


  
function getSelfUrl() {
	var protocol = document.location.protocol;
	var host = document.location.host;
	return protocol + '//' + host;
}