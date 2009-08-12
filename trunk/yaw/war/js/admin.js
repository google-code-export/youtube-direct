jQuery(document).ready( function() {
	if (window.isLoggedIn) {		
		jQuery('#tabs').tabs();		
		init();
	}
});

function init() {
	getAllSubmissions();
}

function test(data) {
	var gridimgpath = '/css/ext/images';
	
	var grid = {};
	grid.datatype = 'local';
	grid.height = 400;
	grid.multiselect = false;
	grid.caption = 'Video Submissions';
	
	grid.colNames = [];
	grid.colModel = [];
	
	grid.colNames.push('Video ID');
	grid.colModel.push({name: 'videoId', index: 'videoId', width: 100, sorttype: 'string'});
	
	grid.colNames.push('Article URL');
	grid.colModel.push({name: 'articleUrl', index: 'articleUrl', width: 200, sorttype: 'string'});
	
	grid.colNames.push('Assignment ID');
	grid.colModel.push({name: 'articleId', index: 'articleId', width: 100, sorttype: 'string'});
	
	grid.colNames.push('Title');
	grid.colModel.push({name: 'title', index: 'title', width: 100, sorttype: 'string'});
	
	grid.colNames.push('Description');
	grid.colModel.push({name: 'description', index: 'description', width: 100, sorttype: 'string'});
	
	grid.colNames.push('Tags');
	grid.colModel.push({name: 'tags', index: 'tags', width: 100, sorttype: 'string'});
	
	grid.colNames.push('Uploader');
	grid.colModel.push({name: 'uploader', index: 'uploader', width: 100, sorttype: 'string'});
	
	grid.colNames.push('Updated');
	grid.colModel.push({name: 'updated', index: 'updated', width: 100, sorttype: 'date'});
	
	grid.colNames.push('Approval Status');	
	grid.colModel.push({name: 'status', index: 'status', width: 100, sorttype: 'int'});
		
	jQuery("#datagrid").jqGrid(grid);
	
	for(var i = 0; i <= data.length; i++) {
		jQuery("#datagrid").addRowData(i + 1, data[i]); 	
	}
}
	
function getAllSubmissions() {
	var url = '/GetAllSubmissions';
	var ajaxCall = {};
	ajaxCall.cache = false;
	ajaxCall.type = 'GET';
	ajaxCall.url = url;
	ajaxCall.dataType = 'json';
	ajaxCall.success = function(json) {
		console.log(json);
		test(json);		
	};
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
