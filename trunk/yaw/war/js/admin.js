window.submissions = [];

jQuery(document).ready( function() {
	if (window.isLoggedIn) {
		jQuery('#tabs').tabs();
		init();
	}
});

function init() {

	getAllSubmissions( function(entries) {
		initDataGrid(entries);
	});

	jQuery('#searchText').keyup( function() {
		var text = jQuery(this).val();
		var matches = filterSubmissions(text);
		refreshGridUI(matches);
	});

}

function filterSubmissions(text) {

	var ret = [];

	var regex = new RegExp(text, 'i');

	for ( var i = 0; i < window.submissions.length; i++) {
		var entry = window.submissions[i];

		var title = entry.videoTitle;
		var description = entry.videoDescription;
		var tags = entry.videoTags;

		if (regex.test(title) || regex.test(description) || regex.test(tags)) {
			ret.push(entry);
		}
	}

	return ret;
}

function initDataGrid(data) {
	var grid = {};
	grid.datatype = 'local';
	grid.height = 300;
	grid.multiselect = false;
	grid.caption = 'Submissions';
	grid.rowNum = 10;
	grid.rowList = [ 10, 20, 30 ];

	grid.colNames = [];
	grid.colModel = [];

	grid.colNames.push('Entry ID');
	grid.colModel.push( {
	  name : 'id',
	  index : 'id',
	  width : 100,
	  hidden : true,
	  sorttype : 'string'
	});

	grid.colNames.push('Created');
	grid.colModel.push( {
	  name : 'created',
	  index : 'created',
	  width : 180,
	  sortype : 'date',
	  formatter : function(cellvalue, options, rowObject) {
		  var date = new Date(cellvalue);
		  return date.toLocaleTimeString() + ' ' + date.toDateString();
	  },
	  sorttype : 'date'
	});

	grid.colNames.push('Video ID');
	grid.colModel.push( {
	  name : 'videoId',
	  index : 'videoId',
	  width : 100,
	  editable : true,
	  hidden : true,
	  sorttype : 'string'
	});

	grid.colNames.push('Assignment ID');
	grid.colModel.push( {
	  name : 'assignmentId',
	  index : 'assignmentId',
	  width : 100,
	  hidden : true,
	  sorttype : 'string'
	});

	grid.colNames.push('Article URL');
	grid.colModel.push( {
	  name : 'articleUrl',
	  index : 'articleUrl',
	  width : 80,
	  formatter : function(cellvalue, options, rowObject) {
		  return '<a href="' + cellvalue + '" target="_blank">link</a>';
	  },
	  align : 'center',
	  sorttype : 'string'
	});

	grid.colNames.push('Uploader');
	grid.colModel.push( {
	  name : 'uploader',
	  index : 'uploader',
	  width : 100,
	  hidden : true,
	  sorttype : 'string'
	});

	grid.colNames.push('Email');
	grid.colModel.push( {
	  name : 'notifyEmail',
	  index : 'notifyEmail',
	  width : 100,
	  sorttype : 'string'
	});

	grid.colNames.push('Video Title');
	grid.colModel.push( {
	  name : 'videoTitle',
	  index : 'videoTitle',
	  width : 100,
	  sorttype : 'string',
	  editable : true,
	  edittype : 'text'
	});

	grid.colNames.push('Video Description');
	grid.colModel.push( {
	  name : 'videoDescription',
	  index : 'videoDescription',
	  width : 200,
	  editable : true,
	  edittype : 'textarea',
	  editoptions : {
	    rows : '3',
	    cols : '30'
	  },
	  sorttype : 'string'
	});

	grid.colNames.push('Video Tags');
	grid.colModel.push( {
	  name : 'videoTags',
	  index : 'videoTags',
	  width : 130,
	  editable : true,
	  edittype : 'text',
	  sorttype : 'string'
	});

	grid.colNames.push('Status');
	grid.colModel.push( {
	  name : 'status',
	  index : 'status',
	  width : 100,
	  edittype : 'select',
	  editable : true,
	  editoptions : {
		  value : '0:UNREVIEWED;1:APPROVED;2:REJECTED'
	  },
	  formatter : function(cellvalue, options, rowObject) {
		  return statusToString(cellvalue);
	  },
	  sorttype : 'string'
	});

	grid.colNames.push('Preview');
	grid.colModel.push( {
	  name : 'preview',
	  index : 'preview',
	  width : 75,
	  align : 'center',
	  sortable : false,
	  sorttype : 'string'
	});

	grid.colNames.push('Delete');
	grid.colModel.push( {
	  name : 'delete',
	  index : 'delete',
	  width : 75,
	  align : 'center',
	  sortable : false,
	  sorttype : 'string'
	});

	grid.afterInsertRow = function(rowid, rowdata, rowelem) {
		var videoId = getVideoId(rowid);
		var previewButton = '<input type="button" onclick=previewVideo("' + videoId + '") value="preview" />';
		jQuery('#datagrid').setCell(rowid, 'preview', previewButton);

		var entryId = getEntryId(rowid);
		var deleteButton = '<input type="button" onclick=deleteEntry("' + entryId + '") value="delete" />';
		jQuery('#datagrid').setCell(rowid, 'delete', deleteButton);
	};

	grid.cellsubmit = 'clientArray';

	grid.editurl = 'server.php';
	grid.autoWidth = true;
	grid.cellEdit = true;
	grid.afterSaveCell = function(rowid, cellname, value, iRow, iCol) {
		// save entry as JDO		
		var entryId = getEntryId(rowid);
		var submission = getSubmission(entryId);

		if (typeof (submission[cellname]) != 'undefined') {
			submission[cellname] = value;
		}
		updateSubmission(submission);
	};

	grid.pager = jQuery('#pager');

	var jqGrid = jQuery('#datagrid').jqGrid(grid);

	for ( var i = 0; i <= data.length; i++) {
		jqGrid.addRowData(i + 1, data[i]);
	}

	jqGrid.navGrid('#pager', {
	  edit : false,
	  add : false,
	  del : false,
	  search : false,
	  refresh : false
	}).navButtonAdd('#pager', {
	  caption : "Refresh",
	  onClickButton : function() {
		  refreshGrid();
	  },
	  position : "last"
	});

}

function getSubmission(id) {
	var ret = null;

	for ( var i = 0; i < submissions.length; i++) {
		var submission = submissions[i];
		if (submission.id == id) {
			ret = submission;
			break;
		}
	}

	return ret;
}

function getEntryId(rowid) {
	return jQuery("#datagrid").getCell(rowid, 0);
}

function getVideoId(rowid) {
	return jQuery("#datagrid").getCell(rowid, 2);
}

function refreshGrid() {
	getAllSubmissions( function(entries) {
		//entries = preProcessData(entries);
		refreshGridUI(entries);
	});
}

function refreshGridUI(entries) {
	var jqGrid = jQuery('#datagrid').clearGridData();
	for ( var i = 0; i <= entries.length; i++) {
		jqGrid.addRowData(i + 1, entries[i]);
	}
}

function deleteEntry(entryId) {
	if (confirm("Delete this entry?")) {
		var url = '/DeleteSubmission?id=' + entryId;
		var ajaxCall = {};
		ajaxCall.cache = false;
		ajaxCall.type = 'GET';
		ajaxCall.url = url;
		ajaxCall.dataType = 'text';
		ajaxCall.success = function(res) {
			refreshGrid();
		};
		jQuery.ajax(ajaxCall);
	}
}

function previewVideo(videoId) {

	var width = 275;
	var height = 250;

	jQuery.ui.dialog.defaults.bgiframe = true;
	var div = jQuery('<div/>');
	div.html(getVideoHTML(videoId, width, height));
	div.dialog();
}

function preProcessData(data) {

	data = data;

	for ( var i = 0; i < data.length; i++) {
		var entry = data[i];
		// entry.status = statusToString(entry.status);
		entry.videoTags = JSON.parse(entry.videoTags).join(',');
	}

	return data;
}

function postProcessEntry(entry) {
	entry.videoTags = JSON.stringify(entry.videoTags.split(','));
	delete entry.updated; // TODO gson can't parse date by default
	delete entry.preview; // don't include the button
	return entry;
}

function statusToString(status) {

	var newStatus = status;

	if (/^[0-2]$/.test(status)) {
		switch (status) {
		case 0:
			newStatus = 'UNREVIEWED';
			break;
		case 1:
			newStatus = 'APPROVED';
			break;
		case 2:
			newStatus = 'REJECTED';
			break;
		}
	}

	if (newStatus == 'UNREVIEWED') {
		newStatus = '<b>UNREVIEWED</b>';
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
		window.submissions = JSON.parse(JSON.stringify(entries));
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

function getVideoHTML(videoId, width, height) {

	width = width || 250;
	height = height || 250;

	var youtubeUrl = 'http://www.youtube.com/v/' + videoId;

	var html = [];
	html.push('<object width="' + width + '" height="' + height + '">');
	html.push('<param name="movie" value="');
	html.push(youtubeUrl);
	html.push('&hl=en&fs=1&"></param>');
	html.push('<param name="allowFullScreen" value="true"></param>');
	html.push('<param name="allowscriptaccess" value="always"></param>');
	html.push('<embed src="');
	html.push(youtubeUrl);
	html.push('&hl=en&fs=1&" type="application/x-shockwave-flash"');
	html.push(' allowscriptaccess="always" allowfullscreen="true" width="'
	    + width + '" height="' + height + '">');
	html.push('</embed></object>');

	return html.join('');
}
