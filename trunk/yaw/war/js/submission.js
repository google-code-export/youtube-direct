// namespace protection against collision
var admin = admin || {};
admin.sub = admin.sub || {};

// user current setting
admin.sub.total = 0; // size of current working set
admin.sub.submissions = []; // current working set
admin.sub.sortBy = 'created';
admin.sub.sortOrder = 'desc';
admin.sub.pageIndex = 1; 
admin.sub.pageSize = 3; 
admin.sub.filterType = 3; // ALL

admin.sub.init = function() {
  admin.sub.initSubmissionGrid();  
  admin.sub.initControlPanel();  
  admin.sub.initSubmissionFilters();  
  
  jQuery('#searchText').keyup( function() {
    var text = jQuery(this).val();
    var matches = admin.sub.filterSubmissions(text);
    admin.sub.refreshGridUI(matches);
  });   
};

admin.sub.initSubmissionFilters = function() {
  var labels = jQuery('#filters a');
  for(var i=0; i<labels.length; i++) {    
    var label = jQuery(labels[i]);     
    admin.sub.setupLabelFilter(label);
  }  
};

admin.sub.setupLabelFilter = function(label) {
  label.click(function() {    
    
    // reset all label colors
    var labels = jQuery('#filters a');
    for(var i=0; i<labels.length; i++) {    
      var label_ = jQuery(labels[i]);     
      label_.css('background', 'white');
      label_.css('color', 'black');
    }     
    
    // set the selected label to be black
    label.css('background', 'black');
    label.css('color', 'white');     
    
    switch (label.html()) {
      case 'UNREVIEWED':
        admin.sub.filterType = 0;
        break;
      case 'APPROVED':
        admin.sub.filterType = 1;
        break;
      case 'REJECTED':
        admin.sub.filterType = 2;
        break;        
      case 'ALL':
        admin.sub.filterType = 3;
        break;        
    }    
    
    // reset the page index to first page
    admin.sub.refreshGrid();                    
  });     
  
  if (label.html() == "ALL") {
    label.css('background', 'black');
    label.css('color', 'white');      
  }
};

admin.sub.initControlPanel = function() {
  jQuery('#refreshGrid').click(function() {
    admin.sub.refreshGrid();
  });
  
  jQuery('#nextPage').click(function() {
    admin.sub.pageIndex++;          
    admin.sub.refreshGrid();
  });  
  
  jQuery('#prevPage').click(function() {
    admin.sub.pageIndex--;          
    admin.sub.refreshGrid();
  });    
};

admin.sub.hasNextPage = function() {
  var totalPages = Math.ceil(admin.sub.total/admin.sub.pageSize);
  if (admin.sub.pageIndex < totalPages) {
    return true;
  } else {
    return false;
  }
};

admin.sub.hasPrevPage = function() {
  if (admin.sub.pageIndex > 1) {
    return true;
  } else {
    return false;
  }
};

admin.sub.filterSubmissions = function(text) {

  var ret = [];

  var regex = new RegExp(text, 'i');

  for ( var i = 0; i < admin.sub.submissions.length; i++) {
    var entry = admin.sub.submissions[i];

    var title = entry.videoTitle;
    var description = entry.videoDescription;
    var tags = entry.videoTags;

    if (regex.test(title) || regex.test(description) || regex.test(tags)) {
      ret.push(entry);
    }
  }

  return ret;
}

admin.sub.initSubmissionGrid = function() {
  var grid = {};
  grid.datatype = 'local';
  grid.height = 100;
  grid.multiselect = false;
  grid.pgbuttons = false;  
  grid.caption = 'Submissions';

  grid.cellsubmit = 'clientArray';  
  grid.autowidth = true;  
  grid.cellEdit = true;   
  
  admin.sub.initGridModels(grid);
  
  grid.afterInsertRow = function(rowid, rowdata, rowelem) {
    var entryId = admin.sub.getEntryId(rowid);
    
    var previewButton = '<input type="button" onclick=admin.sub.previewVideo("' + 
        entryId + '") value="preview" />';
    jQuery('#submissionGrid').setCell(rowid, 'preview', previewButton);
    
    var deleteButton = '<input type="button" onclick=admin.sub.deleteEntry("' + 
        entryId + '") value="delete" />';
    jQuery('#submissionGrid').setCell(rowid, 'delete', deleteButton);

    var downloadButton = '<input type="button" onclick=admin.sub.downloadVideo("' + 
        entryId + '") value="download" />';
    jQuery('#submissionGrid').setCell(rowid, 'download', downloadButton);    
    
    if (rowdata['viewCount'] > 0) {
      var viewCountLink = 
          '<a title="Click to download YouTube Insight data." href="/InsightDownloadRedirect?id=' + 
          rowdata['videoId'] + '&token=' + rowdata['authSubToken'] + '">' + 
          rowdata['viewCount'] + '</a>';
      jQuery('#submissionGrid').setCell(rowid, 'viewCount', viewCountLink);
    }   

  };

  grid.afterSaveCell = function(rowid, cellname, value, iRow, iCol) {
    // save entry as JDO    
    var entryId = admin.sub.getEntryId(rowid);
    var submission = admin.sub.getSubmission(entryId);

    if (typeof (submission[cellname]) != 'undefined') {
      submission[cellname] = value;
    }
    admin.sub.updateSubmission(submission);
  };  
  
  grid.onSortCol = function(colType, columnIndex, sortOrder) {    
    admin.sub.sortBy = colType;
    admin.sub.sortOrder = sortOrder;
    admin.sub.pageIndex = 1;
    admin.sub.refreshGrid();
  };
  
  jQuery('#submissionGrid').jqGrid(grid);
  
  // populate data;
  admin.sub.refreshGrid();
};

admin.sub.initGridModels = function(grid) {
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
  
  // TODO: Need to write unformatter so jqgrid can sort it, now it's unsortable.
  grid.colNames.push('Created');
  grid.colModel.push( {
    name : 'created',
    index : 'created',
    width : 120,
    sortype : 'date',
    formatter : function(cellvalue, options, rowObject) {
      var date = new Date(cellvalue);
      return admin.sub.formatDate(date);
    },
    sorttype : 'date'
  });

  grid.colNames.push('Video ID');
  grid.colModel.push( {
    name : 'videoId',
    index : 'videoId',
    width : 100,
    editable : false,
    hidden : true,
    sorttype : 'string'
  });

  grid.colNames.push('Assignment');
  grid.colModel.push( {
    name : 'assignmentId',
    index : 'assignmentId',
    width : 80,
    hidden : false,
    sorttype : 'string'
  });

  grid.colNames.push('Article');
  grid.colModel.push( {
    name : 'articleUrl',
    index : 'articleUrl',
    width : 50,
    formatter : function(cellvalue, options, rowObject) {
      return '<a title="' + cellvalue + '" href="' + cellvalue + '" target="_blank">link</a>';
    },
    align : 'center',
    sorttype : 'string'
  });

  grid.colNames.push('Username');
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
    width : 70,
    sorttype : 'string'
  });

  grid.colNames.push('Video Title');
  grid.colModel.push( {
    name : 'videoTitle',
    index : 'videoTitle',
    width : 100,
    sorttype : 'string',
    edittype : 'text'
  });

  grid.colNames.push('Video Description');
  grid.colModel.push( {
    name : 'videoDescription',
    index : 'videoDescription',
    width : 150,
    hidden: true,
    edittype : 'text',
    sorttype : 'string'
  });

  grid.colNames.push('Video Tags');
  grid.colModel.push( {
    name : 'videoTags',
    index : 'videoTags',
    width : 100,
    edittype : 'text',
    sorttype : 'string'
  });
  
  grid.colNames.push('View Count');
  grid.colModel.push( {
    name : 'viewCount',
    index : 'viewCount',
    width : 80,
    sorttype : 'int',
    formatter : function(cellvalue, options, rowObject) {
      if (cellvalue < 0) {
        return 'no data';
      } else {        
        //TODO: Figure out why this needs to be a string value.
        return '' + cellvalue;
      }
    }
  });

  grid.colNames.push('Video Source');
  grid.colModel.push( {
    name : 'videoSource',
    index : 'videoSource',
    width : 100,
    edittype : 'text',
    formatter : function(cellvalue, options, rowObject) {
      var ret = null;
      switch (cellvalue) {
      case 0:
        ret = 'New Upload';
        break;
      case 1:
        ret = 'Existing Video';
        break;
      }      
      return ret;
    },
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
      return admin.sub.statusToString(cellvalue);
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

  grid.colNames.push('Download');
  grid.colModel.push( {
    name : 'download',
    index : 'download',
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
    hidden: true,
    sorttype : 'string'
  });  
  
};

admin.sub.formatDate = function(date) {
  var year = admin.sub.padZero(date.getFullYear());
  var month = admin.sub.padZero(date.getMonth() + 1);
  var day = admin.sub.padZero(date.getDate());
  var hours = admin.sub.padZero(date.getHours());
  var minutes = admin.sub.padZero(date.getMinutes());
  var seconds = admin.sub.padZero(date.getSeconds());
  
  return year + '-' + month + '-' + day + ' ' + hours + ':' + minutes + ':' + seconds;
};

admin.sub.padZero = function(value) {
  value = value + '';
  if (value.length < 2) {
    return '0' + value;
  } else {
    return value;
  }
};

admin.sub.getSubmission = function(id) {
  var ret = null;

  for ( var i = 0; i < admin.sub.submissions.length; i++) {
    var submission = admin.sub.submissions[i];
    if (submission.id == id) {
      ret = submission;
      break;
    }
  }

  return ret;
};

admin.sub.getEntryId = function(rowid) {
  return jQuery("#submissionGrid").getCell(rowid, 0);
};

admin.sub.getVideoId = function(rowid) {
  return jQuery("#submissionGrid").getCell(rowid, 2);
};

admin.sub.refreshGrid = function() {
  admin.sub.getAllSubmissions( function(entries) {
    
    admin.sub.refreshGridUI(entries);
    jQuery('#submissionGrid').setCaption('Submissions (' + admin.sub.total + ')');    
        
    jQuery('#pageIndex').html('page ' + admin.sub.pageIndex);
    
    if (admin.sub.hasNextPage()) {
      jQuery('#nextPage').get(0).disabled = false;
    } else {
      jQuery('#nextPage').get(0).disabled = true;
      
    }

    if (admin.sub.hasPrevPage()) {
      jQuery('#prevPage').get(0).disabled = false;
    } else {
      jQuery('#prevPage').get(0).disabled = true; 
    }   
    
  });
};

admin.sub.refreshGridUI = function(entries) {
  var jqGrid = jQuery('#submissionGrid').clearGridData();
  for ( var i = 0; i < entries.length; i++) {
    jqGrid.addRowData(i + 1, entries[i]);
  }
};

admin.sub.downloadVideo = function(entryId) {
  var submission = admin.sub.getSubmission(entryId);
  var videoId = submission.videoId;
  document.location.href = '/VideoDownloadRedirect?id=' + videoId;   
};

admin.sub.deleteEntry = function(entryId) {
  if (confirm("Delete this entry?")) {
    var url = '/DeleteSubmission?id=' + entryId;
    var ajaxCall = {};
    ajaxCall.cache = false;
    ajaxCall.type = 'GET';
    ajaxCall.url = url;
    ajaxCall.dataType = 'text';
    ajaxCall.success = function(res) {
      admin.sub.refreshGrid();
    };
    jQuery.ajax(ajaxCall);
  }
};

admin.sub.previewVideo = function(entryId) {
  
  var submission = admin.sub.getSubmission(entryId);
  var videoId = submission.videoId;
  var title = submission.videoTitle;

  var videoWidth = 275;
  var videoHeight = 250;

  var dialogOptions = {};
  dialogOptions.title = title;
  dialogOptions.width = 400;
  dialogOptions.height = 300;
  
  jQuery.ui.dialog.defaults.bgiframe = true;
  var div = jQuery('<div/>');
  div.html(admin.sub.getVideoHTML(videoId, videoWidth, videoHeight));
  div.dialog(dialogOptions);
};

admin.sub.statusToString = function(status) {

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
};

admin.sub.stringToStatus = function(str) {

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
};

admin.sub.getAllSubmissions =function(callback) {
  var url = '/GetAllSubmissions?sortby=' + admin.sub.sortBy + '&sortorder=' +  admin.sub.sortOrder + 
      '&pageindex=' + admin.sub.pageIndex + '&pagesize=' + admin.sub.pageSize + 
      '&filtertype=' + admin.sub.filterType;
  console.log(url);
  var ajaxCall = {};
  ajaxCall.cache = false;
  ajaxCall.type = 'GET';
  ajaxCall.url = url;
  ajaxCall.dataType = 'json';
  ajaxCall.success = function(result) {  
    admin.sub.total = result.total;
    var entries = result.entries                  
    admin.sub.submissions = entries.concat([]);
    admin.sub.showLoading(false);
    callback(entries);        
  };
  admin.sub.showLoading(true);
  jQuery.ajax(ajaxCall);
};

admin.sub.updateSubmission = function(entry) {
  var url = '/UpdateSubmission';
  var ajaxCall = {};
  ajaxCall.type = 'POST';
  ajaxCall.url = url;
  ajaxCall.data = JSON.stringify(entry);
  ajaxCall.cache = false;
  ajaxCall.processData = false;
  ajaxCall.success = function(res) {
    admin.sub.showLoading(false);
  };

  admin.sub.showLoading(true, 'saving ...');
  jQuery.ajax(ajaxCall);

};

admin.sub.showLoading = function(status, text) {
  if (status) {
    text = text || 'loading ...';
    jQuery('#submissionStatus').html(text).show();
  } else {
    jQuery('#submissionStatus').html('').hide();
  }
};

admin.sub.getVideoHTML = function(videoId, width, height) {

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
};
