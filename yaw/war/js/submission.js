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

// namespace protection against collision
var admin = admin || {};
admin.sub = admin.sub || {};

// user current setting
admin.sub.total = 0; // size of current working set
admin.sub.submissions = []; // current working set
admin.sub.sortBy = 'created';
admin.sub.sortOrder = 'desc';
admin.sub.pageIndex = 1; 
admin.sub.pageSize = 20; 
admin.sub.filterType = 'ALL';

admin.sub.init = function() {
  admin.sub.initSubmissionGrid();  
  admin.sub.initControlPanel();  
  admin.sub.initSubmissionFilters();  
  
  jQuery('#submissionSearchText').keyup( function() {
    admin.sub.filterByText();
  });   
};

admin.sub.initSubmissionFilters = function() {
  var labels = jQuery('#submissionFilters a.filter');
  for(var i=0; i<labels.length; i++) {    
    var label = jQuery(labels[i]);     
    admin.sub.setupLabelFilter(label);
  }  
};

admin.sub.setupLabelFilter = function(label) {
  label.click(function() {    
    
    // reset all label colors
    var labels = jQuery('#submissionFilters a.filter');
    for(var i=0; i<labels.length; i++) {    
      var label_ = jQuery(labels[i]);     
      label_.css('background', 'white');
      label_.css('color', 'black');
    }     
    
    // set the selected label to be highlighted
    label.css('background', '#a6c9e2');
    label.css('color', 'black');     
    
    admin.sub.filterType = label.html();
          
    // reset the page index to first page
    admin.sub.pageIndex = 1;
    
    admin.sub.refreshGrid();                    
    
  });     
  
  if (label.html() == "ALL") {
    label.css('background', '#a6c9e2');
    label.css('color', 'black');      
  }
};

admin.sub.initControlPanel = function() {
  jQuery('#submissionRefreshGrid').click(function() {
    admin.sub.refreshGrid();
  });
  
  jQuery('#submissionNextPage').click(function() {
    admin.sub.pageIndex++;          
    admin.sub.refreshGrid();
  });  
  
  jQuery('#submissionPrevPage').click(function() {
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

admin.sub.filterByText = function() {

  var matches = [];

  var text = jQuery('#submissionSearchText').val();   
  
  var regex = new RegExp(text, 'i');

  for ( var i = 0; i < admin.sub.submissions.length; i++) {
    var entry = admin.sub.submissions[i];

    var title = entry.videoTitle;
    var description = entry.videoDescription;
    var tags = entry.videoTags;

    if (regex.test(title) || regex.test(description) || regex.test(tags)) {
      matches.push(entry);
    }
  }
  
  admin.sub.refreshGridUI(matches); 
}

admin.sub.initSubmissionGrid = function() {
  var grid = {};
  grid.datatype = 'local';
  grid.height = 500;
  grid.multiselect = false;
  grid.pgbuttons = false;  
  grid.caption = 'Submissions';

  grid.cellsubmit = 'clientArray';  
  //grid.autowidth = true;  
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
    
    var detailsButton = '<input type="button" onclick=admin.sub.showDetails("' + 
    entryId + '") value="details" />';
    jQuery('#submissionGrid').setCell(rowid, 'details', detailsButton);     
    
    if (rowdata['viewCount'] > 0) {
      var viewCountLink = 
          '<a title="Click to download YouTube Insight data." href="/admin/InsightDownloadRedirect?id=' + 
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
  grid.colNames.push('Created on');
  grid.colModel.push( {
    name : 'created',
    index : 'created',
    width : 120,
    sortype : 'date',
    formatter : function(cellvalue, options, rowObject) {
      var date = new Date(cellvalue);
      return admin.sub.formatDate(date);
    }
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
      var link = '';    
      if (cellvalue) { 
        link = '<a title="' + cellvalue + '" href="' + cellvalue + '" target="_blank">link</a>';
      }
      return link;
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
    hidden : true,
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
    sorttype : 'string',
    hidden: true
  });
  
  grid.colNames.push('View Count');
  grid.colModel.push( {
    name : 'viewCount',
    index : 'viewCount',
    width : 80,
    sorttype : 'int',
    hidden: true,
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
    width : 110,
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
      value : 'UNREVIEWED:UNREVIEWED;APPROVED:APPROVED;REJECTED:REJECTED;SPAM:SPAM'
    },
    sorttype : 'string'
  });

  grid.colNames.push('Preview');
  grid.colModel.push( {
    name : 'preview',
    index : 'preview',
    width : 75,
    align : 'center',
    sortable : false
  });

  grid.colNames.push('Download');
  grid.colModel.push( {
    name : 'download',
    index : 'download',
    width : 75,
    align : 'center',
    sortable : false,
    hidden: true
  }); 
  
  grid.colNames.push('Delete');
  grid.colModel.push( {
    name : 'delete',
    index : 'delete',
    width : 75,
    align : 'center',
    sortable : false,
    hidden: true
  });  

  grid.colNames.push('Details');
  grid.colModel.push( {
    name : 'details',
    index : 'details',
    width : 75,
    align : 'center',
    sortable : false,
    hidden: false
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
  return jQuery('#submissionGrid').getCell(rowid, 0);
};

admin.sub.getVideoId = function(rowid) {
  return jQuery('#submissionGrid').getCell(rowid, 2);
};

admin.sub.getTotalPage = function() {
  return Math.ceil(admin.sub.total / admin.sub.pageSize);
};

admin.sub.refreshGrid = function() {
  admin.sub.getAllSubmissions( function(entries) {
    
    admin.sub.refreshGridUI(entries);
    
    var captionTitle = null;
    
    switch(admin.sub.filterType) {
      case 'ALL':
        captionTitle = 'All Submissions';   
        break;
      case 'UNREVIEWED':
        captionTitle = 'Unreview Submissions';
        break;
      case 'APPROVED':
        captionTitle = 'Approved Submissions';
        break;
      case 'REJECTED':
        captionTitle = 'Rejected Submissions';
        break;
      case 'SPAM':
        captionTitle = 'Spam Submissions';
        break;        
    }
    
    jQuery('#submissionGrid').setCaption(captionTitle + ' (' + admin.sub.total + ')');      
    
    var totalPage = admin.sub.getTotalPage();
    if (totalPage > 0) {
      jQuery('#submissionPageIndex').html('Page ' + admin.sub.pageIndex + ' of ' + totalPage);
    } else {
      jQuery('#submissionPageIndex').html('0 result');
    }
    
    if (admin.sub.hasNextPage()) {
      jQuery('#submissionNextPage').get(0).disabled = false;
    } else {
      jQuery('#submissionNextPage').get(0).disabled = true;
      
    }

    if (admin.sub.hasPrevPage()) {
      jQuery('#submissionPrevPage').get(0).disabled = false;
    } else {
      jQuery('#submissionPrevPage').get(0).disabled = true; 
    }   
    
  });
};

admin.sub.refreshGridUI = function(entries) {
  var jqGrid = jQuery('#submissionGrid').clearGridData();
  for ( var i = 0; i < entries.length; i++) {
    jqGrid.addRowData(i + 1, entries[i]);
  }
};

admin.sub.showDetails = function(entryId) {
  var submission = admin.sub.getSubmission(entryId);
  
  var mainDiv = jQuery('#submissionDetailsTemplate').clone();   
  
  var videoWidth = 255;
  var videoHeight = 220;  
  
  var dialogOptions = {};
  dialogOptions.title = submission.videoTitle;
  dialogOptions.width = 700;
  dialogOptions.height = 650;
  
  jQuery.ui.dialog.defaults.bgiframe = true;
  
  var videoHtml = admin.sub.getVideoHTML(submission.videoId, videoWidth, videoHeight);
  
  mainDiv.css('display', 'block');
  
  mainDiv.find('#assignmentId').html(submission.assignmentId);
  
  var created = new Date(submission.created).toLocaleTimeString() + ' ' + 
      new Date(submission.created).toLocaleDateString()
  
  mainDiv.find('#created').html(created);
  
  mainDiv.find('#videoSource').html(submission.videoSource);  
  
  var creatorInfo = submission.youTubeName + 
      (submission.notifyEmail?' (' + submission.notifyEmail +')':'');    
  mainDiv.find('#youTubeName').html(creatorInfo);  
  
  mainDiv.find('#videoId').html(
      '<a target="_blank" href="http://www.youtube.com/watch?v=' + 
      submission.videoId + '">' + 
      submission.videoId + '</a>');   
  mainDiv.find('#youTubeState').html(submission.youTubeState);
  
  mainDiv.find('#videoTitle').html(submission.videoTitle);
  
  mainDiv.find('#videoDescription').html(submission.videoDescription);
  
  mainDiv.find('#videoTags').html(submission.videoTags);  
  
  var articleLink = submission.articleUrl?'<a target="_blank" href="' + 
      submission.articleUrl + '">' + submission.articleUrl + '</a>':'N/A';  
  mainDiv.find('#articleUrl').html(articleLink);  
  
  mainDiv.find('#videoDate').html(
      submission.videoDate?submission.videoDate:'N/A');
  
  mainDiv.find('#videoLocation').html(
      submission.videoLocation?submission.videoLocation:'N/A');
  
  mainDiv.find('#video').html(videoHtml);
  
  var moderationStatus = -1;
  switch(submission.status) {
    case 'UNREVIEWED':
      moderationStatus = 0;
      break;
    case 'APPROVED':
      moderationStatus = 1;
      break;
    case 'REJECTED':
      moderationStatus = 2;
      break;
    case 'SPAM':
      moderationStatus = 3;
      break;      
  }
    
  mainDiv.find('#moderationStatus').get(0).selectedIndex = moderationStatus;  
  mainDiv.find('#moderationStatus').change(function() {
    switch(mainDiv.find('#moderationStatus').get(0).selectedIndex) {
      case 0:
        submission.status = 'UNREVIEWED';
        break;
      case 1:
        submission.status = 'APPROVED';
        break;
      case 2:
        submission.status = 'REJECTED';
        break;
      case 3:
        submission.status = 'SPAM';
        break;        
    }
    admin.sub.updateSubmission(submission);
  });    
  
  mainDiv.find('#adminNotes').html(submission.adminNotes);
  
  mainDiv.find('#saveAdminNotes').click(function() {
    submission.adminNotes = mainDiv.find('#adminNotes').val();
    admin.sub.updateSubmission(submission);
  });
  
  mainDiv.find('#download').click(function() {
    admin.sub.downloadVideo(submission);
  });  
  
  mainDiv.dialog(dialogOptions);
};

admin.sub.downloadVideo = function(submission) {
  console.log(submission);
  document.location.href = '/admin/VideoDownloadRedirect?id=' + submission.videoId + 
  '&username=' + submission.youTubeName;   
};

admin.sub.deleteEntry = function(entryId) {
  if (confirm("Delete this entry?")) {
    var url = '/admin/DeleteSubmission?id=' + entryId;
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
  dialogOptions.width = 330;
  dialogOptions.height = 300;
  
  jQuery.ui.dialog.defaults.bgiframe = true;
  var div = jQuery('<div align="center"/>');
  div.html(admin.sub.getVideoHTML(videoId, videoWidth, videoHeight));
  div.dialog(dialogOptions);
};

admin.sub.getAllSubmissions =function(callback) {
  var url = '/admin/GetAllSubmissions?sortby=' + admin.sub.sortBy + '&sortorder=' +  admin.sub.sortOrder + 
      '&pageindex=' + admin.sub.pageIndex + '&pagesize=' + admin.sub.pageSize;
  
  if (admin.sub.filterType != 'ALL') {
    url += '&filtertype=' + admin.sub.filterType;
  }
  //console.log(url);
  
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
  var url = '/admin/UpdateSubmission';
  var ajaxCall = {};
  ajaxCall.type = 'POST';
  ajaxCall.url = url;
  ajaxCall.data = JSON.stringify(entry);
  ajaxCall.cache = false;
  ajaxCall.processData = false;
  ajaxCall.success = function(res) {
    admin.sub.showLoading(false);
    admin.sub.refreshGrid();
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
