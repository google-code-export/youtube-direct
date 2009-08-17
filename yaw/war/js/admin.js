jQuery(document).ready( function() {
	if (window.isLoggedIn) {		
		jQuery('#tabs').tabs();		
		init();
	}
});

function init() {
	getAllSubmissions(function(entries) {
		entries = preProcessData(entries);	
		initDataGrid(entries);
	});	
}

function initDataGrid(data) {	
	var grid = {};
	grid.datatype = 'local';
	grid.height = 300;
	grid.multiselect = false;
	grid.caption = 'Video Submissions';
	grid.rowNum = 10;
	grid.rowList = [10,20,30]; 
	
	grid.colNames = [];
	grid.colModel = [];
	
	grid.colNames.push('Last Updated');
	grid.colModel.push({
		name: 'updated', 
		index: 'updated', 
		width: 155, 
		sorttype: 'date'});
	
	grid.colNames.push('Video ID');
	grid.colModel.push({
		name: 'videoId', 
		index: 'videoId', 
		width: 100, 
		hidden: true,
		sorttype: 'string'});

	grid.colNames.push('Assignment ID');
	grid.colModel.push({
		name: 'assignmentId', 
		index: 'assignmentId', 
		width: 100, 
		hidden: true,
		sorttype: 'string'});	
	
	grid.colNames.push('Article URL');
	grid.colModel.push({
		name: 'articleUrl', 
		index: 'articleUrl', 
		width: 100, 
		formatter: 'link',
		sorttype: 'string'});	
	
	grid.colNames.push('Uploader');
	grid.colModel.push({
		name: 'uploader', 
		index: 'uploader', 
		width: 100, 
		sorttype: 'string'});
	
	grid.colNames.push('Email');
	grid.colModel.push({
		name: 'notifyEmail', 
		index: 'notifyEmail', 
		width: 100, 
		sorttype: 'string'});	
	
	grid.colNames.push('Video Title');
	grid.colModel.push({
		name: 'videoTitle', 
		index: 'videoTitle', 
		width: 100, 
		sorttype: 'string', 
		editable: true,		
		edittype: 'text'});
	
	grid.colNames.push('Video Description');
	grid.colModel.push({
		name: 'videoDescription', 
		index: 'videoDescription', 
		width: 150,
		editable: true,
		edittype: 'textarea',
		editoptions: {rows:'3', cols: '30'},
		sorttype: 'string'});
	
	grid.colNames.push('Video Tags');
	grid.colModel.push({
		name: 'videoTags', 
		index: 'videoTags', 
		width: 150, 
		editable: true,
		edittype: 'text',		
		sorttype: 'string'});		
	
	grid.colNames.push('Approval Status');	
	grid.colModel.push({
		name: 'status', 
		index: 'status', 
		width: 100, 
		sorttype: 'string',			
		edittype: 'select',
		editable: true,
		editoptions: {value: '0:unreviewed;1:approved;2:rejected'}});

	grid.colNames.push('Preview');	
	grid.colModel.push({
		name: 'preview', 
		index: 'preview', 
		width: 75, 
		sortable: false,
		sorttype: 'string'});		

	
	grid.afterInsertRow = function(rowid, rowdata, rowelem) {
		var videoId = jQuery("#datagrid").getCell(rowid, 1);									
		var button = '<input type="button" onclick=previewVideo("' + 
			videoId + '") value="preview" />';		
		jQuery('#datagrid').setCell(rowid, 'preview', button);
	};
	
	grid.cellsubmit = 'clientArray';
	
	grid.editurl = 'server.php';
	grid.autoWidth = true;
	grid.cellEdit = true;
	grid.afterSaveCell  = function(rowid, cellname, value, iRow, iCol) {
		// save entry as JDO		
		var entry = jQuery('#datagrid').getRowData(rowid);
		entry = postProcessEntry(entry);
		updateSubmission(entry);
	};
	
	grid.pager = jQuery('#pager');	
	
	var jqGrid = jQuery('#datagrid').jqGrid(grid);
	
	for(var i = 0; i <= data.length; i++) {
		jqGrid.addRowData(i + 1, data[i]); 	
	}

	jqGrid.navGrid('#pager', {edit:false,add:false,del:false,search:false,refresh: false})
	.navButtonAdd('#pager',{
	   caption:"Refresh", 
	   onClickButton: function(){
		 
	     getAllSubmissions(function(entries) {	    	 
	    	 jqGrid.clearGridData();
	    	 entries = preProcessData(entries);
	    	 for(var i = 0; i <= entries.length; i++) {
	    		 jqGrid.addRowData(i + 1, entries[i]); 	
	    	 }
	     });
	   }, 
	   position:"last"
	});
	
	
}

function previewVideo(videoId) {
	jQuery.ui.dialog.defaults.bgiframe = true;	
	var div = jQuery('<div/>');
	div.html(getVideoHTML(videoId));
	div.dialog();		
}

function preProcessData(data) {	
	
	data = data;
	
	for (var i = 0; i < data.length; i++) {
		var entry = data[i];
		entry.status = statusToString(entry.status);		
		entry.updated = new Date(entry.updated);
		entry.videoTags = JSON.parse(entry.videoTags).join(',');
	}
	
	return data;
}

function postProcessEntry(entry) {
	entry.status = stringToStatus(entry.status);
	entry.videoTags = JSON.stringify(entry.videoTags.split(','));	
	delete entry.updated; // TODO gson can't parse date by default
	delete entry.preview; // don't include the button		
	return entry;
}

function statusToString(status) {
	
	var newStatus = 'unreviewed';
	
	switch (status) {
		case 0:
			newStatus = 'unreviewed';
			break;
		case 1:
			newStatus = 'approved';
			break;
		case 2:
			newStatus = 'rejected';
			break;
	}
	
	return newStatus;	
}

function stringToStatus(str) {
	
	var status = 0;
	
	switch (str) {
		case 'unreviewed':
			status = 0;
			break;
		case 'approved':
			status = 1;
			break;
		case 'rejected':
			status = 2;
			break;
	}
	
	return status;	
}

function getAllSubmissions(callback) {
	var url = '/GetAllSubmissions';
	var ajaxCall = {};
	ajaxCall.cache = false;
	ajaxCall.type = 'GET';
	ajaxCall.url = url;
	ajaxCall.dataType = 'json';
	ajaxCall.success = function(entries) {
		console.log(entries);
		showLoading(false);
		callback(entries);
	};	
	showLoading(true);
	jQuery.ajax(ajaxCall);	
}

function updateSubmission(entry) {
	var url = '/UpdateSubmission';
    var ajaxCall = {};
    ajaxCall.type = 'POST';
    ajaxCall.url = url;
    ajaxCall.data = JSON.stringify(entry);
    ajaxCall.cache = false;
    ajaxCall.processData = false;
    ajaxCall.success = function(res) { 
      showLoading(false);
      console.log(res);
    };
    
    showLoading(true, 'saving ...');
    jQuery.ajax(ajaxCall);	
	
}

function showLoading(status, text) {
	if (status) {		
		text = text || 'loading ...';		
		jQuery('#status').html(text);		
	} else {
		jQuery('#status').html('&nbsp;');	
	}	
}

function getVideoHTML(videoId) {
	
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
	
	return html.join('');
}
