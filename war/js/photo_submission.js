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
admin.photo = admin.photo || {};

// user current setting
admin.photo.total = 0; // size of current working set
admin.photo.submissions = []; // current working set
admin.photo.sortBy = 'created';
admin.photo.sortOrder = 'desc';
admin.photo.pageIndex = 1;
admin.photo.pageSize = 20;
admin.photo.filterType = 'ALL';

admin.photo.init = function() {
  admin.photo.initSubmissionGrid();
  admin.photo.initControlPanel();

  jQuery('#photoSearchText').keyup( function() {
    admin.photo.filterByText();
  });
};

admin.photo.initControlPanel = function() {
  jQuery('#photoRefreshGrid').click( function() {
    admin.photo.refreshGrid();
  });

  jQuery('#photoNextPage').click( function() {
    admin.photo.pageIndex++;
    admin.photo.refreshGrid();
  });

  jQuery('#photoPrevPage').click( function() {
    admin.photo.pageIndex--;
    admin.photo.refreshGrid();
  });
};

admin.photo.hasNextPage = function() {
  var totalPages = Math.ceil(admin.photo.total / admin.photo.pageSize);
  if (admin.photo.pageIndex < totalPages) {
    return true;
  } else {
    return false;
  }
};

admin.photo.hasPrevPage = function() {
  if (admin.photo.pageIndex > 1) {
    return true;
  } else {
    return false;
  }
};

admin.photo.filterByText = function() {
  var matches = [];

  var text = jQuery('#photoSearchText').val();

  var regex = new RegExp(text, 'i');

  for ( var i = 0; i < admin.photo.submissions.length; i++) {
    var entry = admin.photo.submissions[i];

    var title = entry.title;
    var description = entry.description;

    if (regex.test(title) || regex.test(description)) {
      matches.push(entry);
    }
  }

  admin.photo.refreshGridUI(matches);
}

admin.photo.deleteEntry = function(entryId) {
  if (confirm("This will delete the entire photo submission along with all its photos.  Are you sure?")) {
    admin.photo.deleteSubmission(entryId, function() {
      var messageElement = admin.showMessage("Deleting photo submission...");
      admin.photo.refreshGrid();
    });
  }
};

admin.photo.initSubmissionGrid = function() {
  var grid = {};
  grid.datatype = 'local';
  grid.height = 500;
  grid.multiselect = false;
  grid.pgbuttons = false;
  grid.caption = 'Photo Submission';

  grid.cellsubmit = 'clientArray';
  // grid.autowidth = true;
  grid.cellEdit = true;

  admin.photo.initGridModels(grid);

  grid.afterInsertRow = function(rowid, rowdata, rowelem) {
    var entryId = admin.photo.getEntryId(rowid);

    var detailsButton = '<input type="button" onclick=admin.photo.showDetails("' + entryId + '") value="details" />';
    jQuery('#photoGrid').setCell(rowid, 'details', detailsButton);

    var deleteButton = '<input type="button" onclick=admin.photo.deleteEntry("' + entryId + '") value="delete" />';
    jQuery('#photoGrid').setCell(rowid, 'delete', deleteButton);
  };

  grid.afterSaveCell = function(rowid, cellname, value, iRow, iCol) {
    // save entry as JDO    
    var entryId = admin.photo.getEntryId(rowid);
    var submission = admin.photo.getSubmission(entryId);

    if (typeof (submission[cellname]) != 'undefined') {
      submission[cellname] = value;
    }
    admin.photo.updateSubmissionStatus(submission);
  };

  grid.onSortCol = function(colType, columnIndex, sortOrder) {
    admin.photo.sortBy = colType;
    admin.photo.sortOrder = sortOrder;
    admin.photo.pageIndex = 1;
    admin.photo.refreshGrid();
  };

  jQuery('#photoGrid').jqGrid(grid);

  // populate data;
  admin.photo.refreshGrid();
};

admin.photo.initGridModels = function(grid) {
  grid.colNames = [];
  grid.colModel = [];

  grid.colNames.push('Photo ID');
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
      return admin.photo.formatDate(date);
    }
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
        link = '<a title="' + cellvalue + '" href="' + cellvalue
            + '" target="_blank">link</a>';
      }
      return link;
    },
    align : 'center',
    sorttype : 'string'
  });

  grid.colNames.push('Email');
  grid.colModel.push( {
    name : 'notifyEmail',
    index : 'notifyEmail',
    width : 70,
    hidden : false,
    sorttype : 'string'
  });

  grid.colNames.push('Title');
  grid.colModel.push( {
    name : 'title',
    index : 'title',
    width : 100,
    sorttype : 'string',
    edittype : 'text'
  });

  grid.colNames.push('Description');
  grid.colModel.push( {
    name : 'description',
    index : 'description',
    width : 150,
    hidden : false,
    edittype : 'text',
    sorttype : 'string'
  });

  grid.colNames.push('Photo Count');
  grid.colModel.push( {
    name : 'numberOfPhotos',
    index : 'numberOfPhotos',
    width : 80,
    sorttype : 'int'
  });

  grid.colNames.push('Details');
  grid.colModel.push( {
    name : 'details',
    index : 'details',
    width : 75,
    align : 'center',
    sortable : false,
    hidden : false
  });

  grid.colNames.push('Delete');
  grid.colModel.push( {
    name : 'delete',
    index : 'delete',
    width : 75,
    align : 'center',
    sortable : false,
    hidden : false
  });
};

admin.photo.formatDate = function(date) {
  var year = admin.photo.padZero(date.getFullYear());
  var month = admin.photo.padZero(date.getMonth() + 1);
  var day = admin.photo.padZero(date.getDate());
  var hours = admin.photo.padZero(date.getHours());
  var minutes = admin.photo.padZero(date.getMinutes());
  var seconds = admin.photo.padZero(date.getSeconds());

  return year + '-' + month + '-' + day + ' ' + hours + ':' + minutes + ':'
      + seconds;
};

admin.photo.padZero = function(value) {
  value = value + '';
  if (value.length < 2) {
    return '0' + value;
  } else {
    return value;
  }
};

admin.photo.getSubmission = function(id) {
  var ret = null;

  for ( var i = 0; i < admin.photo.submissions.length; i++) {
    var submission = admin.photo.submissions[i];
    if (submission.id == id) {
      ret = submission;
      break;
    }
  }

  return ret;
};

admin.photo.getEntryId = function(rowid) {
  return jQuery('#photoGrid').getCell(rowid, 0);
};

admin.photo.getVideoId = function(rowid) {
  return jQuery('#photoGrid').getCell(rowid, 2);
};

admin.photo.getTotalPage = function() {
  return Math.ceil(admin.photo.total / admin.photo.pageSize);
};

admin.photo.refreshGrid = function() {
  admin.photo.getAllSubmissions( function(entries) {

    admin.photo.refreshGridUI(entries);

    var totalPage = admin.photo.getTotalPage();
    if (totalPage > 0) {
      jQuery('#photoPageIndex').html(
          'Page ' + admin.photo.pageIndex + ' of ' + totalPage);
    } else {
      jQuery('#photoPageIndex').html('0 result');
    }

    if (admin.photo.hasNextPage()) {
      jQuery('#photoNextPage').get(0).disabled = false;
    } else {
      jQuery('#photoNextPage').get(0).disabled = true;

    }

    if (admin.photo.hasPrevPage()) {
      jQuery('#photoPrevPage').get(0).disabled = false;
    } else {
      jQuery('#photoPrevPage').get(0).disabled = true;
    }

  });
};

admin.photo.refreshGridUI = function(entries) {
  var jqGrid = jQuery('#photoGrid').clearGridData();
  for ( var i = 0; i < entries.length; i++) {
    jqGrid.addRowData(i + 1, entries[i]);
  }
};

admin.photo.resizeImage = function(image, max) {
  var width = image.width;
  var height = image.height;

  if (width > max || height > max) {
    var wRatio = width / max;
    var hRatio = height / max;

    var ratio = Math.max(wRatio, hRatio);

    var w = Math.round(width / ratio);
    var h = Math.round(height / ratio);

    image.width = w;
    image.height = h;
    image.style.width = w;
    image.style.height = h;

    image.style.pixelHeight = h;
    // image.offsetHeight = h;

    image.style.pixelWidth = w;
    // image.offsetWidth = w;
  }
  return image;
};

admin.photo.getImageThumb = function(entry) {
  var topMostDiv = jQuery('<div id="' + entry.id + '_main" style="padding: 1px;"/>');
  
  var checkboxDiv = jQuery('<div id="checkbox"/>');
  var thumbnailDiv = jQuery('<div style="width: 120px; height: 100px" id="thumbnail"/>');
  var photoModerationDiv = jQuery('<div id="photoModeration"/>');
  
  var thumbDiv = jQuery('<div id="' + entry.id + '"/>');

  var thumbImage = jQuery('<img/>');
  thumbImage.attr('src', entry.thumbnailUrl);

  thumbImage.click( function() {
    var img = new Image();
    img.src = entry.imageUrl;

    img.onload = function() {
      var options = {};
      img = admin.photo.resizeImage(img, 800);

      options.width = img.width + 50;
      options.height = img.height + 100;
      
      var popUp = jQuery('<div align="center"/>');
      popUp.css('width', options.width);
      popUp.css('height', options.height);
      
      options.open = function() {
        var imageLink = jQuery('<a/>');
        imageLink.attr('href', img.src);
        imageLink.attr('target', '_blank');
        imageLink.html('Open in new window');
  
        popUp.append(imageLink);
        popUp.append('<br><br>');
        popUp.append(img);        
      };
      
      options.close = function() {
        jQuery(img).remove();
        img = null;
      };
      
      jQuery.ui.dialog.defaults.bgiframe = true;
      popUp.dialog(options);
    };
  });
  
  thumbDiv.append(thumbImage);

  var checkbox = jQuery('<input type="checkbox" value="' + entry.id + '">');
  
  var photoModeration = jQuery('<select id="' + entry.id + '_mod"/>');
  photoModeration.append('<option value="none"> ------ </option>');
  photoModeration.append('<option value="approve">approved</option>');
  photoModeration.append('<option value="reject">rejected</option>');
  
  photoModeration.change(function() {
    var selection = jQuery(photoModeration.get(0)[photoModeration.get(0).selectedIndex]);
    
    switch(selection.val()) {      
      case 'approve':
        admin.photo.updateStatus([entry.id], 'APPROVED', function() {
      thumbDiv.fadeTo('fast', 1);
        });      
        break;
      case 'reject':
        admin.photo.updateStatus([entry.id], 'REJECTED', function() {
          thumbDiv.fadeTo('fast', .3);          
        });          
        break;             
    }
  });

  switch (entry.status.toUpperCase()) {
  case 'APPROVED':
    thumbDiv.fadeTo('fast', 1);
    photoModeration.get(0).selectedIndex = 1;
    break;
  case 'REJECTED':
    thumbDiv.fadeTo('fast', 0.3);
    photoModeration.get(0).selectedIndex = 2;
    break;
  }
  
  checkboxDiv.append(checkbox);
  thumbnailDiv.append(thumbDiv);
  photoModerationDiv.append(photoModeration);
  
  topMostDiv.append(checkboxDiv);
  topMostDiv.append(thumbnailDiv);
  topMostDiv.append(photoModerationDiv);
  
  return topMostDiv;
};

admin.photo.showDetails = function(entryId) {
  var submission = admin.photo.getSubmission(entryId);
  var div = jQuery('<div style="width: 700px; height: 600;" align="center"/>');
  
  var dialogOptions = {};
  dialogOptions.title = submission.title;
  dialogOptions.width = 700;
  dialogOptions.height = 700;
  dialogOptions.open = function() {
    var mainDiv = jQuery('#photoDetailsTemplate').clone();
    mainDiv.css('display', 'block');
    mainDiv.find('#assignmentId').html(submission.assignmentId);
  
    var created = new Date(submission.created).toLocaleTimeString() + ' '
        + new Date(submission.created).toLocaleDateString()
  
    mainDiv.find('#created').html(created);
  
    mainDiv.find('#author').html(submission.author);
  
    mainDiv.find('#title').html(submission.title);
  
    mainDiv.find('#description').html(submission.description);
    
    mainDiv.find('#phoneNumber').html(
        submission.phoneNumber?submission.phoneNumber:'N/A');  
    
    var articleLink = submission.articleUrl ? '<a target="_blank" href="'
        + submission.articleUrl + '">' + submission.articleUrl + '</a>' : 'N/A';
    mainDiv.find('#articleUrl').html(articleLink);
  
    mainDiv.find('#location').html(
        submission.location ? submission.location : 'N/A');
  
    var colSize = 3;
    
    // Grab photo entries
    admin.photo.getAllPhotos(submission.id, function(entries) {
      var photoHtml = [];
      var photosTable = mainDiv.find('#photos');    
      
      var rowSize = Math.floor(entries.length / colSize) + entries.length % colSize;
      
      var entryIndex = 0;
      
      for (var i = 0; i < rowSize; i++) {
        var tr = jQuery('<tr/>');
        photosTable.append(tr);            
        
        for (var j = 0; j < colSize; j++) {
          if (entryIndex >= entries.length) {
            break;
          }
        
          var imageThumb = admin.photo.getImageThumb(entries[entryIndex]);
          var td = jQuery('<td valign="top"/>');
          td.append(imageThumb);
          tr.append(td);
          entryIndex++;
        } 
      }
    });
  
    mainDiv.find('#selectAll').click( function() {
      var checkboxes = mainDiv.find('#photos').find('input:checkbox');
      for ( var i = 0; i < checkboxes.length; i++) {
        checkboxes[i].checked = this.checked;
      }
    });
    
    var approvedFeedLinkHtml = [];
    approvedFeedLinkHtml.push('[ <a target="_blank" href="');
    approvedFeedLinkHtml.push('/approved_photos/atom?id=' + entryId + '">');
    approvedFeedLinkHtml.push('atom');
    approvedFeedLinkHtml.push('</a> ]');
    approvedFeedLinkHtml.push('&nbsp;');
    approvedFeedLinkHtml.push('[ <a target="_blank" href="');
    approvedFeedLinkHtml.push('/approved_photos/json?id=' + entryId + '">');
    approvedFeedLinkHtml.push('json');
    approvedFeedLinkHtml.push('</a> ]');  
    
    mainDiv.find('#feedsDiv').html(approvedFeedLinkHtml.join(''));
    
    mainDiv.find('#photoBulkAction').change(function() {
      var selection = mainDiv.find('#photoBulkAction').get(0)[mainDiv.find('#photoBulkAction').get(0).selectedIndex];    
      var checkboxes = mainDiv.find('#photos').find('input:checked');
  
      if (checkboxes.length == 0) {
        mainDiv.find('#photoBulkAction').get(0).selectedIndex = 0;
        return;
      }
  
      switch (selection.value) {
        case 'approve':
          var ids = [];
          for ( var i = 0; i < checkboxes.length; i++) {
            ids.push(jQuery(checkboxes[i]).val());
          }
    
          admin.photo.updateStatus(ids, 'APPROVED', function() {
            for ( var i = 0; i < ids.length; i++) {
              var id = ids[i];
              var thumbnailDiv = jQuery(mainDiv.find('#photos').find('#' + id));
              thumbnailDiv.fadeTo('fast', 1);
              var moderationSelect = jQuery(mainDiv.find('#photos').find('#' + id + '_mod'));
              moderationSelect.get(0).selectedIndex = 1;
            }
          });
          break;
        case 'reject':
          var ids = [];
          for ( var i = 0; i < checkboxes.length; i++) {
            ids.push(jQuery(checkboxes[i]).val());
          }
    
          admin.photo.updateStatus(ids, 'REJECTED', function() {
            for ( var i = 0; i < ids.length; i++) {
              var id = ids[i];
              var thumbnailDiv = jQuery(mainDiv.find('#photos').find('#' + id));
              thumbnailDiv.fadeTo('slow', .3);          
              var moderationSelect = jQuery(mainDiv.find('#photos').find('#' + id + '_mod'));
              moderationSelect.get(0).selectedIndex = 2;
            }
          });
          break;
        case 'delete':
          var ids = [];
          for ( var i = 0; i < checkboxes.length; i++) {
            ids.push(jQuery(checkboxes[i]).val());
          }
    
          if (confirm('This will delete all selected images permanently.  Are you sure?')) {
            admin.photo.deletePhotos(ids, function() {
              for ( var i = 0; i < ids.length; i++) {
                var id = ids[i];
                var thumbnailDiv = jQuery(mainDiv.find('#photos').find('#' + id + '_main'));
                thumbnailDiv.remove();
              }
              admin.photo.refreshGrid();
            });
          }
          break;
      }
  
      // Reset all selections
      for ( var i = 0; i < checkboxes.length; i++) {
        checkboxes[i].checked = false;
      }
      mainDiv.find('#photoBulkAction').get(0).selectedIndex = 0;
      mainDiv.find('#selectAll').get(0).checked = false;
    });
  
    mainDiv.find('#adminNotes').html(submission.adminNotes);
  
    mainDiv.find('#saveAdminNotes').click( function() {
      var command = 'UPDATE_PHOTO_SUBMISSION_ADMIN_NOTES';
      var params = {};
      params.id = submission.id;
      params.adminNotes = mainDiv.find('#adminNotes').val();
  
      var jsonRpcCallback = function(jsonStr) {
        try {
          var json = JSON.parse(jsonStr);
          if (!json.error) {
            submission.adminNotes = params.adminNotes;
            alert('Notes are saved.');
          } else {
            admin.showError(json.error, messageElement);
          }
        } catch (exception) {
          admin.showError('Request failed: ' + exception, messageElement);
        }
      }
  
      jsonrpc.makeRequest(command, params, jsonRpcCallback);
    });
    
    div.append(mainDiv);
  };

  jQuery.ui.dialog.defaults.bgiframe = true;
  div.dialog(dialogOptions);
};

admin.photo.updateStatus = function(ids, status, callback) {
  var messageElement = admin.showMessage("Updating photo entries...");

  var command = 'UPDATE_PHOTO_ENTRIES_STATUS';
  var params = {};
  params.ids = ids.join(',');
  params.status = status;

  var jsonRpcCallback = function(jsonStr) {
    try {
      var json = JSON.parse(jsonStr);
      if (!json.error) {
        admin.showMessage("Photo entries updated.", messageElement);
        var entries = json.result;
        callback(entries);
      } else {
        admin.showError(json.error, messageElement);
      }
    } catch (exception) {
      admin.showError('Request failed: ' + exception, messageElement);
    }
  }

  jsonrpc.makeRequest(command, params, jsonRpcCallback);
};

admin.photo.deletePhotos = function(ids, callback) {
  var messageElement = admin.showMessage("Deleting photo entries...");

  var command = 'DELETE_PHOTO_ENTRIES';
  var params = {};
  params.ids = ids.join(',');

  var jsonRpcCallback = function(jsonStr) {
    try {
      var json = JSON.parse(jsonStr);
      if (!json.error) {
        admin.showMessage("Photo entries deleted.", messageElement);
        var entries = json.result;
        callback(entries);
      } else {
        admin.showError(json.error, messageElement);
      }
    } catch (exception) {
      admin.showError('Request failed: ' + exception, messageElement);
    }
  }

  jsonrpc.makeRequest(command, params, jsonRpcCallback);
};

admin.photo.deleteSubmission = function(id, callback) {
  var messageElement = admin.showMessage("Deleting photo submission...");

  var command = 'DELETE_PHOTO_SUBMISSION';
  var params = {};
  params.id = id

  var jsonRpcCallback = function(jsonStr) {
    try {
      var json = JSON.parse(jsonStr);
      if (!json.error) {
        admin.showMessage("Photo submission deleted.", messageElement);
        var entries = json.result;
        callback(entries);
      } else {
        admin.showError(json.error, messageElement);
      }
    } catch (exception) {
      admin.showError('Request failed: ' + exception, messageElement);
    }
  }

  jsonrpc.makeRequest(command, params, jsonRpcCallback);
};

admin.photo.getAllPhotos = function(submissionId, callback) {
  var messageElement = admin.showMessage("Loading photo entries...");

  var command = 'GET_ALL_PHOTO_ENTRIES';
  var params = {};
  params.submissionId = submissionId;

  var jsonRpcCallback = function(jsonStr) {
    try {
      var json = JSON.parse(jsonStr);
      if (!json.error) {
        admin.showMessage("Photo entries loaded.", messageElement);
        var entries = json.result;
        callback(entries);
      } else {
        admin.showError(json.error, messageElement);
      }
    } catch (exception) {
      admin.showError('Request failed: ' + exception, messageElement);
    }
  }

  jsonrpc.makeRequest(command, params, jsonRpcCallback);
};

admin.photo.getAllSubmissions = function(callback) {
  var messageElement = admin.showMessage("Loading photo submission...");

  var command = 'GET_PHOTO_SUBMISSIONS';
  var params = {};
  params.sortBy = admin.photo.sortBy;
  params.sortOrder = admin.photo.sortOrder;
  params.pageIndex = admin.photo.pageIndex;
  params.pageSize = admin.photo.pageSize;

  var jsonRpcCallback = function(jsonStr) {
    try {
      var json = JSON.parse(jsonStr);
      if (!json.error) {
        admin.showMessage("Photo submission loaded.", messageElement);
        admin.photo.total = json.totalSize;
        var entries = json.result;
        admin.photo.submissions = entries.concat( []);
        callback(entries);
      } else {
        admin.showError(json.error, messageElement);
      }
    } catch (exception) {
      admin.showError('Request failed: ' + exception, messageElement);
    }
  }

  jsonrpc.makeRequest(command, params, jsonRpcCallback);
};

admin.photo.updateSubmissionStatus = function(entry) {
  var messageElement = admin.showMessage("Updating submission status...");

  var command = 'UPDATE_PHOTO_ENTRY_STATUS';
  var params = {};
  params.id = entry.id;
  params.status = entry.status;

  var jsonRpcCallback = function(jsonStr) {
    try {
      var json = JSON.parse(jsonStr);
      if (!json.error) {
        admin.showMessage("Photo entry status updated.", messageElement);
        admin.photo.refreshGrid();
      } else {
        admin.showError(json.error, messageElement);
      }
    } catch (exception) {
      admin.showError('Request failed: ' + exception, messageElement);
    }
  }

  jsonrpc.makeRequest(command, params, jsonRpcCallback);
};

admin.photo.getVideoHTML = function(videoId, width, height) {
  var width = width || 250;
  var height = height || 250;

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

admin.photo.imageResize = function(image) {
  var max = 100;
  var w_ = image.width;
  var h_ = image.height;

  if (w_ > max || h_ > max) {
    var wRatio = w_ / max;

    var hRatio = h_ / max;

    var ratio = Math.max(wRatio, hRatio);

    var w = Math.round(w_ / ratio);
    var h = Math.round(h_ / ratio);

    image.width = w;
    image.height = h;
    image.style.width = w;
    image.style.height = h;

    image.style.pixelHeight = h;
    // image.offsetHeight = h;

    image.style.pixelWidth = w;
    // image.offsetWidth = w;

  }
  ;

  return image;
}
