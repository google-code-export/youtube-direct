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
admin.assign = admin.assign || {};

// user current setting
admin.assign.total = 0; // size of current working set
admin.assign.assignments = []; // current working set
admin.assign.sortBy = 'created';
admin.assign.sortOrder = 'desc';
admin.assign.pageIndex = 1; 
admin.assign.pageSize = 20; 
admin.assign.filterType = 'ALL';

admin.assign.init = function() {
  admin.assign.initAssignmentGrid();  
  admin.assign.initControlPanel();  
  admin.assign.initAssignmentFilters();
  
  jQuery('#assignmentSearchText').keyup(function() {
    admin.assign.filterByText();
  });    
  
  jQuery('#assignmentCreateButton').click(function() {
  	admin.assign.loadYouTubeCategories();
  });
};

admin.assign.loadYouTubeCategories = function() {
  var messageElement = admin.showMessage("Loading YouTube categories...");

  var command = 'GET_YOUTUBE_CATEGORIES';

  var jsonRpcCallback = function(json) {
    try {
      if (!json.error) {
        admin.showMessage("YouTube categories loaded.", messageElement);
        admin.assign.showAssignmentCreate(json.categories);
      } else {
        admin.showError(json.error, messageElement);  
      }
    } catch(exception) {
    	admin.showError(exception, messageElement);
    }
  }
  
  jsonrpc.makeRequest(command, {}, jsonRpcCallback);
};

// This is hacky, but I'm trying to retrofit some new functionality and hopefully I can remove this
// distinct method at some point in the future.
admin.assign.loadYouTubeCategoriesForModify = function(assignmentId) {
  var messageElement = admin.showMessage("Loading YouTube categories...");
  
  jsonrpc.makeRequest('GET_YOUTUBE_CATEGORIES', {}, function(json) {
    try {
      if (!json.error) {
        admin.showMessage("YouTube categories loaded.", messageElement);
        admin.assign.loadAllPlaylists(json.categories, assignmentId);
      } else {
        admin.showError(json.error, messageElement);  
      }
    } catch(exception) {
      admin.showError(exception, messageElement);
    }
  });
};

admin.assign.initAssignmentFilters = function() {
  var labels = jQuery('#assignmentFilters a.filter');
  for (var i = 0; i<labels.length; i++) {    
    var label = jQuery(labels[i]);     
    admin.assign.setupLabelFilter(label);
  }  
};

admin.assign.setupLabelFilter = function(label) {
  label.click(function() {
    // reset all label colors
    var labels = jQuery('#assignmentFilters a.filter');
    for (var i = 0; i < labels.length; i++) {
      var label_ = jQuery(labels[i]);
      label_.css('background', 'white');
      label_.css('color', '#black');
    }

    // set the selected label to be highlighted
    label.css('background', '#a6c9e2');
    label.css('color', 'black');

    admin.assign.filterType = label.html();

    // reset the page index to first page
    admin.assign.pageIndex = 1;

    admin.assign.refreshGrid();
  });

  if (label.html() == "ALL") {
    label.css('background', '#a6c9e2');
    label.css('color', 'black');
  }
};

admin.assign.initControlPanel = function() {
  jQuery('#assignmentRefreshGrid').click(function() {
    admin.assign.refreshGrid();
  });
  
  jQuery('#assignmentNextPage').click(function() {
    admin.assign.pageIndex++;          
    admin.assign.refreshGrid();
  });  
  
  jQuery('#assignmentPrevPage').click(function() {
    admin.assign.pageIndex--;          
    admin.assign.refreshGrid();
  });    
};

admin.assign.hasNextPage = function() {
  var totalPages = Math.ceil(admin.assign.total / admin.assign.pageSize);
  if (admin.assign.pageIndex < totalPages) {
    return true;
  } else {
    return false;
  }
};

admin.assign.hasPrevPage = function() {
  if (admin.assign.pageIndex > 1) {
    return true;
  } else {
    return false;
  }
};

admin.assign.filterByText = function() {
  var matches = [];

  var text = jQuery('#assignmentSearchText').val();   
  var regex = new RegExp(text, 'i');

  for (var i = 0; i < admin.assign.assignments.length; i++) {
    var entry = admin.assign.assignments[i];

    var description = entry.description;

    if (regex.test(description)) {
      matches.push(entry);
    }
  }
  
  admin.assign.refreshGridUI(matches); 
}

admin.assign.initAssignmentGrid = function() {
  var grid = {};
  grid.datatype = 'local';
  grid.height = 500;
  grid.multiselect = false;
  grid.pgbuttons = false;  
  grid.caption = 'Assignments';

  grid.cellsubmit = 'clientArray';
  grid.autowidth = true;
  grid.cellEdit = true;   
  
  admin.assign.initGridModels(grid);
  
  grid.afterInsertRow = function(rowid, rowdata, rowelem) {
    var entryId = admin.assign.getEntryId(rowid);

    var embedButton = jQuery.sprintf('<input type="button" onclick=admin.assign.showEmbedCode("%s") value="Embed"/>', entryId);
    jQuery('#assignmentGrid').setCell(rowid, 'embed', embedButton);

    var playlistButton = jQuery.sprintf('<input type="button" onclick=admin.assign.showPlaylistCode("%s") value="Playlist"/>', entryId);
    jQuery('#assignmentGrid').setCell(rowid, 'playlist', playlistButton);
    
    var modifyButton = jQuery.sprintf('<input type="button" onclick=admin.assign.loadYouTubeCategoriesForModify("%s") value="Modify"/>', entryId);
    jQuery('#assignmentGrid').setCell(rowid, 'modify', modifyButton);
  };

  grid.afterSaveCell = function(rowid, cellname, value, iRow, iCol) {
    // save entry as JDO    
    var entryId = admin.assign.getEntryId(rowid);
    var assignment = admin.assign.getAssignment(entryId);

    if (typeof (assignment[cellname]) != 'undefined') {
      assignment[cellname] = value;
    }
    admin.assign.updateAssignment(assignment);
  };  
  
  grid.onSortCol = function(colType, columnIndex, sortOrder) {    
    admin.assign.sortBy = colType;
    admin.assign.sortOrder = sortOrder;
    admin.assign.pageIndex = 1;
    admin.assign.refreshGrid();
  };
  
  jQuery('#assignmentGrid').jqGrid(grid);
  
  // populate data;
  admin.assign.refreshGrid();
};

admin.assign.initGridModels = function(grid) {
  grid.colNames = [];
  grid.colModel = [];
  
  grid.colNames.push('Created On');
  grid.colModel.push({
    name: 'created', 
    index: 'created', 
    width: 150,
    sortype : 'date',
    formatter : function(cellvalue, options, rowObject) {
      var date = new Date(cellvalue);
      return admin.formatDate(date);
    }  
  });  
  
  grid.colNames.push('ID');
  grid.colModel.push({
    name: 'id', 
    index: 'id', 
    width: 60  
  });
  
  grid.colNames.push('Description');
  grid.colModel.push({
    name: 'description', 
    index: 'description', 
    width: 280,
    editable: true,
    edittype: 'text',
    editoptions: {rows:'3', cols: '30'},
    editrules: {required: true}
  });
  
  grid.colNames.push('Title');  
  grid.colModel.push({
    name: 'title',
    index: 'title',
    width: 60,
    sortable: true,
    sorttype : 'string',
    hidden: true
  });

  grid.colNames.push('Category');
  grid.colModel.push( {
    name : 'category',
    index : 'category',
    width : 100,
    editable : false,
    sorttype : 'string',
    sortable: true
  });    
  
  grid.colNames.push('Status');
  grid.colModel.push( {
    name : 'status',
    index : 'status',
    width : 90,
    sorttype : 'string',
    sortable: true
  });  
  
  grid.colNames.push('# of Subs');  
  grid.colModel.push({
    name: 'submissionCount',
    index: 'submissionCount',
    width: 60,
    sortable: false,
    hidden: true
  });
  
  grid.colNames.push('Playlist');
  grid.colModel.push({
    name: 'playlistId', 
    index: 'playlistId', 
    width: 70,
    formatter : function(cellvalue, options, rowObject) {
      var url = 'http://www.youtube.com/view_play_list?p=' + cellvalue;
      return '<a title="' + url + '" href="' + url + '" target="_blank">link</a>';
    },    
    hidden: false
  });

  grid.colNames.push('Embed Code');
  grid.colModel.push( {
    name : 'embed',
    index : 'embed',
    width : 80,
    align : 'center',
    sortable : false
  });  

  grid.colNames.push('Playlist Code');
  grid.colModel.push( {
    name : 'playlist',
    index : 'playlist',
    width : 80,
    align : 'center',
    sortable : false
  });
  
  grid.colNames.push('Modify Assignment');
  grid.colModel.push({
    name : 'modify',
    index : 'modify',
    width : 120,
    align : 'center',
    sortable : false
  });  
};

admin.assign.getSelfUrl = function() {
  var protocol = document.location.protocol;
  var host = document.location.host;
  return protocol + '//' + host;
};

admin.assign.showEmbedCode = function(id) {  
  var entry = admin.assign.getAssignment(id);   
  jQuery.ui.dialog.prototype.options.bgiframe = true;

  var code = [];
  code.push(jQuery.sprintf('<script type="text/javascript" src="%s/js/ytd-embed.js"></script>', admin.assign.getSelfUrl()));
  code.push('<script type="text/javascript">');
  code.push('var ytdInitFunction = function() {');
  code.push('  var ytd = new Ytd();');
  code.push(jQuery.sprintf('  ytd.setAssignmentId("%s");', entry.id));
  code.push(jQuery.sprintf('  ytd.setCallToAction("callToActionId-%s");', entry.id));
  code.push('  var containerWidth = 350;');
  code.push('  var containerHeight = 550;');
  code.push(jQuery.sprintf('  ytd.setYtdContainer("ytdContainer-%s", containerWidth, containerHeight);', entry.id));
  code.push('  ytd.ready();');
  code.push('};');
  code.push('if (window.addEventListener) {');
  code.push('  window.addEventListener("load", ytdInitFunction, false);');
  code.push('} else if (window.attachEvent) {');
  code.push('  window.attachEvent("onload", ytdInitFunction);');
  code.push('}');
  code.push('</script>');
  code.push('//Modify the next two lines to customize the Call to Action button.');
  code.push(jQuery.sprintf('<a id="callToActionId-%s" href="javascript:void(0);"><img src="%s/images/calltoaction.png"></a>', entry.id, admin.assign.getSelfUrl()));
  code.push(jQuery.sprintf('<div id="ytdContainer-%s"></div>', entry.id));  
  
  code = code.join('\n');
  code = code.replace(/\</g,'&lt;');
  code = code.replace(/\>/g,'&gt;');
  
  var dialogContainer = jQuery('<pre class="preDialog">');
  dialogContainer.html(code);
  
  var dialogOptions = {};
  dialogOptions.title = 'Embed Code';
  dialogOptions.width = 550;
  dialogOptions.height = 'auto';
   
  dialogContainer.dialog(dialogOptions);
};

admin.assign.showPlaylistCode = function(id) {  
  var entry = admin.assign.getAssignment(id); 
  var playlistId = entry.playlistId;
  
  jQuery.ui.dialog.prototype.options.bgiframe = true;

  var code = [];
  var dialogContainer;
  
  if (playlistId == null || playlistId == '') {
  	dialogContainer = jQuery('<div>');

    code = 'No playlist is associated with this assignment. If you just created the assignment, ' +
    	'try waiting a few seconds and then refeshing. Otherwise, please check the YouTube account ' +
      'settings in the "Configuration" tab.';
  } else {
    var width = 480;
    var height = 385;
    var playlistUrl = jQuery.sprintf('http://www.youtube.com/p/%s?fs=1', playlistId);
    
  	dialogContainer = jQuery('<pre class="preDialog">');
  	
    code.push(jQuery.sprintf('<object width="%d" height="%d">', width, height));
    code.push(jQuery.sprintf('  <param name="movie" value="%s"></param>', playlistUrl));
    code.push('  <param name="allowFullScreen" value="true"></param>');
    code.push('  <param name="allowscriptaccess" value="always"></param>');
    code.push(jQuery.sprintf('  <embed src="%s" type="application/x-shockwave-flash" allowscriptaccess="always" allowfullscreen="true" width="%d" height="%d"></embed>', playlistUrl, width, height));
    code.push('</object>');
    
    code = code.join('\n');
    code = code.replace(/\</g,'&lt;');
    code = code.replace(/\>/g,'&gt;');
  }

  dialogContainer.html(code);
  
  var dialogOptions = {};
  dialogOptions.title = 'Playlist Code';
  dialogOptions.width = 550;
  dialogOptions.height = 'auto';  
   
  dialogContainer.dialog(dialogOptions);
};

admin.assign.getAssignment = function(id) {
  var ret = null;

  for (var i = 0; i < admin.assign.assignments.length; i++) {
    var assignment = admin.assign.assignments[i];
    if (assignment.id == id) {
      ret = assignment;
      break;
    }
  }

  return ret;
};

admin.assign.getEntryId = function(rowid) {
  return jQuery("#assignmentGrid").getCell(rowid, 1);
};

admin.assign.getVideoId = function(rowid) {
  return jQuery("#assignmentGrid").getCell(rowid, 2);
};

admin.assign.getTotalPage = function() {
  return Math.ceil(admin.assign.total / admin.assign.pageSize);
};

admin.assign.refreshGrid = function() {
  admin.assign.getAllAssignments(function(entries) {
    
    admin.assign.refreshGridUI(entries);
    
    var captionTitle = null;
    
    switch(admin.assign.filterType) {
      case 'ALL':
        captionTitle = 'All Assignments';   
        break;
      case 'PENDING':
        captionTitle = 'Pending Assignments';
        break;
      case 'ACTIVE':
        captionTitle = 'Active Assignments';
        break;
      case 'ARCHIVED':
        captionTitle = 'Archived Assignments';
        break;  
    }
    
    jQuery('#assignmentGrid').setCaption(captionTitle + ' (' + admin.assign.total + ')');
    
    var totalPage = admin.assign.getTotalPage();
    if (totalPage > 0) {
      jQuery('#assignmentPageIndex').html('Page ' + admin.assign.pageIndex + ' of ' + totalPage);
    } else {
      jQuery('#assignmentPageIndex').html('0 result');
    }
    
    if (admin.assign.hasNextPage()) {
      jQuery('#assignmentNextPage').get(0).disabled = false;
    } else {
      jQuery('#assignmentNextPage').get(0).disabled = true;
    }

    if (admin.assign.hasPrevPage()) {
      jQuery('#assignmentPrevPage').get(0).disabled = false;
    } else {
      jQuery('#assignmentPrevPage').get(0).disabled = true;
    }
  });
};

admin.assign.refreshGridUI = function(entries) {
  var jqGrid = jQuery('#assignmentGrid').clearGridData();
  for (var i = 0; i < entries.length; i++) {
    jqGrid.addRowData(i + 1, entries[i]);
  }
};

admin.assign.showAssignmentCreate = function(categories) {
  var dialogOptions = {};
  dialogOptions.title = "Create a New Assignment";
  dialogOptions.width = 700;
  dialogOptions.height = 'auto';
  
  jQuery.ui.dialog.prototype.options.bgiframe = true; 
  
  var div = jQuery('#assignmentCreateTemplate').clone();  
  
  var categorySelector = div.find('#assignmentCategories');
  for (var i = 0; i < categories.length; i++) {
    var category = categories[i];
    if (category == 'News') {
    	categorySelector.append('<option value="News" selected="selected">News</option>');
    } else {
    	categorySelector.append(jQuery.sprintf('<option value="%s">%s</option>', category, category));
    }
  }

  div.find('#createCancelButton').click(function() {
    div.dialog('destroy');
  });
  
  div.find('#createButton').click(function() {
    var messageElement = admin.showMessage("Creating assignment...");
    
    var command = 'NEW_ASSIGNMENT';
    var params = {};
    params.description = div.find('#assignmentDescription').val();
    params.title = div.find('#playlistTitle').val();
    params.loginInstruction = div.find('#assignmentLoginInstruction').val();
    params.postSubmitMessage = div.find('#assignmentPostSubmitMessage').val();
    params.category = div.find('#assignmentCategories').get(0).
        options[div.find('#assignmentCategories').attr('selectedIndex')].value;
    params.status = div.find('#assignmentStatusType').get(0).
        options[div.find('#assignmentStatusType').attr('selectedIndex')].value;            
        
    var callback = function(json) {
      try {
        if (!json.error) {
        	admin.showMessage("Assignment created.", messageElement);
        	admin.assign.pageIndex = 1;
        	admin.assign.refreshGrid();
        } else {
          admin.showError("Could not create a new assignment: " + json.error, messageElement);
        }
      } catch(exception) {
      	admin.showError(exception, messageElement);
      }
    } 
    
    jsonrpc.makeRequest(command, params, callback);
    div.dialog('destroy');
  });  
  
  div.dialog(dialogOptions);    
};

admin.assign.loadAllPlaylists = function(categories, assignmentId) {
  var messageElement = admin.showMessage("Loading YouTube playlists...");
  
  jsonrpc.makeRequest('GET_YOUTUBE_PLAYLISTS', {}, function(playlistsJson) {
    try {
      if (!playlistsJson.error) {
        admin.showMessage("Playlists loaded.", messageElement);
        
        admin.assign.loadAllAlbums(categories, assignmentId, playlistsJson);
      } else {
        admin.showError(playlistsJson.error, messageElement);
        
        admin.assign.loadAllAlbums(categories, assignmentId, null);
      }
    } catch(exception) {
      admin.showError(exception, messageElement);
    }
  });
}

admin.assign.loadAllAlbums = function(categories, assignmentId, playlistsJson) {
  var messageElement = admin.showMessage("Loading Picasa albums...");
  
  jsonrpc.makeRequest('GET_PICASA_ALBUMS', {}, function(albumsJson) {
    try {
      if (!albumsJson.error) {
        admin.showMessage("Albums loaded.", messageElement);
        
        admin.assign.showAssignmentModify(categories, assignmentId, playlistsJson, albumsJson);
      } else {
        admin.showError(albumsJson.error, messageElement);
        
        admin.assign.showAssignmentModify(categories, assignmentId, playlistsJson, null);
      }
    } catch(exception) {
      admin.showError(exception, messageElement);
    }
  });
}

admin.assign.showAssignmentModify = function(categories, assignmentId, playlistsJson, albumsJson) {
  var assignment = admin.assign.getAssignment(assignmentId);

  var dialogDiv = jQuery('#assignmentEditTemplate').clone();
  
  dialogDiv.find('#title').html(assignment.title);
  dialogDiv.find('#description').val(assignment.description);
  
  var categorySelector = dialogDiv.find('#category');
  for (var i = 0; i < categories.length; i++) {
    var category = categories[i];
    if (category == assignment.category) {
      categorySelector.append(jQuery.sprintf('<option value="%s" selected="selected">%s</option>', category, category));
    } else {
      categorySelector.append(jQuery.sprintf('<option value="%s">%s</option>', category, category));
    }
  }

  if (playlistsJson != null) {
    var playlistHtml = [];
    jQuery.each(playlistsJson.playlists, function(playlistId, metadata) {
      if (playlistId == assignment.playlistId) {
        playlistHtml.push(jQuery.sprintf('<option id="%s" value="%s" selected="selected">%s</option>', metadata.title, playlistId, metadata.title));
      } else {
        playlistHtml.push(jQuery.sprintf('<option id="%s" value="%s">%s</option>', metadata.title, playlistId, metadata.title));
      }
    });
    dialogDiv.find('#playlist').html(playlistHtml.sort().join());
  }

  if (albumsJson != null) {
    var albumHtml = [];
    jQuery.each(albumsJson.albums, function(albumUrl, metadata) {
      if (albumUrl == assignment.approvedAlbumUrl) {
        albumHtml.push(jQuery.sprintf('<option id="%s" value="%s" selected="selected">%s</option>', metadata.title, albumUrl, metadata.title));
      } else {
        albumHtml.push(jQuery.sprintf('<option id="%s" value="%s">%s</option>', metadata.title, albumUrl, metadata.title));
      }
    });
    dialogDiv.find('#album').html(albumHtml.sort().join());
  }
  
  dialogDiv.find('#modifyCancelButton').click(function() {
    dialogDiv.dialog('destroy');
  });
  
  dialogDiv.find('#modifyButton').click(function() {
    var messageElement = admin.showMessage("Modifying assignment...");
    
    assignment.category = dialogDiv.find('#category').val();
    assignment.status = dialogDiv.find('#status').val();
    assignment.description = dialogDiv.find('#description').val();
    
    if (playlistsJson != null) {
      assignment.playlistId = dialogDiv.find('#playlist').val();
    }
    
    if (albumsJson != null) {
      var approvedAlbumUrl = dialogDiv.find('#album').val();
      assignment.approvedAlbumUrl = approvedAlbumUrl;
      assignment.rejectedAlbumUrl = albumsJson.albums[approvedAlbumUrl].rejectedAlbumUrl;
      assignment.unreviewedAlbumUrl = albumsJson.albums[approvedAlbumUrl].unreviewedAlbumUrl;
    }
    
    jsonrpc.makeRequest('UPDATE_ASSIGNMENT', assignment, function(json) {
      try {
        if (!json.error) {
          admin.showMessage("Assignment modified.", messageElement);
          admin.assign.refreshGrid();
        } else {
          admin.showError("Could not modify assignment: " + json.error, messageElement);
        }
      } catch(exception) {
        admin.showError(exception, messageElement);
      }
    });

    dialogDiv.dialog('destroy');
  });
  
  jQuery.ui.dialog.prototype.options.bgiframe = true;
  dialogDiv.dialog({
    title: "Modify an Assignment",
    width: 700,
    height: 'auto'
  });
}

admin.assign.getAllAssignments = function(callback) {
  var messageElement = admin.showMessage("Loading assignments...");
  
  var command = 'GET_ASSIGNMENTS';
  var params = {};
  params.sortBy = admin.assign.sortBy;
  params.sortOrder = admin.assign.sortOrder;
  params.pageIndex = admin.assign.pageIndex;
  params.pageSize = admin.assign.pageSize;
  params.filterType = admin.assign.filterType;
  
  var jsonRpcCallback = function(json) {
    try {
      if (!json.error) {
        admin.showMessage("Assignments loaded.", messageElement);
        admin.assign.total = json.totalSize;
        var entries = json.result;
        admin.assign.assignments = entries.concat([]);
        callback(entries);  
      } else {
        admin.showError(json.error, messageElement);  
      }
    } catch(exception) {
    	admin.showError(exception, messageElement);
    }
  } 
  
  jsonrpc.makeRequest(command, params, jsonRpcCallback);
};

admin.assign.updateAssignment = function(entry) {
  var messageElement = admin.showMessage("Updating assignment...");
  
  var command = 'UPDATE_ASSIGNMENT';
  var params = entry;
  
  var jsonRpcCallback = function(json) {
    try {
      if (!json.error) {
        admin.showMessage("Assignment updated.", messageElement);
        admin.assign.refreshGrid();
      } else {
        admin.showError(json.error, messageElement);  
      }
    } catch(exception) {
    	admin.showError(exception, messageElement);
    }
  } 
  
  jsonrpc.makeRequest(command, params, jsonRpcCallback);
};
