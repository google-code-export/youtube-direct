jQuery(document).ready( function() {
	console.log(window.isLoggedIn);

	if (window.isLoggedIn) {
		init();
	}
});

function getAllSubmissions() {
	var url = '/GetAllSubmissions';
	var ajaxCall = {};
	ajaxCall.cache = false;
	ajaxCall.type = 'GET';
	ajaxCall.url = url;
	ajaxCall.dataType = 'json';
	ajaxCall.success = function(json) {
		console.log(json);
		displaySubmissions(json);
		jQuery('#status').empty();
	};
	
	jQuery('#status').html('loading ...');
	jQuery.ajax(ajaxCall);
	
}

function displayVideo(videoId) {
	
	jQuery('#videoDisplay').html('loading video ...');
	
	var youtubeUrl = 'http://www.youtube.com/v/' + videoId;
	
	var html = [];
	html.push('<object width="250" height="200">');
	html.push('<param name="movie" value="');
	html.push(youtubeUrl);
	html.push('&hl=en&fs=1&"></param>');
	html.push('<param name="allowFullScreen" value="true"></param>');
	html.push('<param name="allowscriptaccess" value="always"></param>');
	html.push('<embed src="');
	html.push(youtubeUrl);
	html.push('&hl=en&fs=1&" type="application/x-shockwave-flash"');
	html.push(' allowscriptaccess="always" allowfullscreen="true" width="250" height="200">');
	html.push('</embed></object>');	
	
	jQuery('#videoDisplay').html(html.join(''));
}

function displaySubmissions(videos) {
	
	for (var i = 0; i < videos.length; i++) {
		var video = videos[i];
		var videoId = video.videoId;
		var articleId = video.articleId;
		var partnerId = video.partnerId;
		var articleUrl = video.articleUrl;
		var tags = video.tags;
		var title = video.title;
		var description = video.description;
		var uploader = video.uploader;
		var updated = new Date(video.updated);
		var status = video.status;		
					
		var html = [];	
		html.push('<div>uploader id: ' + uploader + '</div>');
		html.push('<div>title: ' + title + '</div>');
		html.push('<div>description: ' + description + '</div>');
		html.push('<div>tags: ' + tags + '</div>');	
		html.push('<div>partner id: ' + partnerId + '</div>');		
		html.push('<div>article url: ' + articleUrl + '</div>');
		html.push('<div>article id: ' + articleId + '</div>');
		html.push('<div>updated: ' + updated + '</div>');
		html.push('<div>status: ' + status + '</div>');
		html.push('<div><a href=\'javascript:displayVideo(\"' + videoId + '\");\'>preview</a></div>');
		html.push('<br><br>')
		
		jQuery('#videoList').append(html.join(''));
	}

}

function init() {
	getAllSubmissions();
}