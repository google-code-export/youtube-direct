// namespace protection against collision
var admin = admin || {};
admin.assign = admin.assign || {};

// TODO fetch the category list from server
admin.assign.ytCategories = 'News,People,Tech,Music,Entertainment,Film';

// user current setting
admin.assign.total = 0; // size of current working set
admin.assign.assignments = []; // current working set
admin.assign.sortBy = 'created';
admin.assign.sortOrder = 'desc';
admin.assign.pageIndex = 1; 
admin.assign.pageSize = 20; 
admin.assign.filterType = -1; // all

admin.assign.init = function() {
  admin.assign.initAssignmentGrid();  
  admin.assign.initControlPanel();  
  admin.assign.initAssignmentFilters();
  
  jQuery('#assignmentSearchText').keyup( function() {
    admin.assign.filterByText();
  });    
  
  jQuery('#assignmentCreateButton').click(function() {
    admin.assign.showAssignmentCreate();
  });
  
};

admin.assign.initAssignmentFilters = function() {
  var labels = jQuery('#assignmentFilters a.filter');
  for(var i=0; i<labels.length; i++) {    
    var label = jQuery(labels[i]);     
    admin.assign.setupLabelFilter(label);
  }  
};

admin.assign.setupLabelFilter = function(label) {
  label.click(function() {    
    
    // reset all label colors
    var labels = jQuery('#assignmentFilters a.filter');
    for(var i=0; i<labels.length; i++) {    
      var label_ = jQuery(labels[i]);     
      label_.css('background', 'white');
      label_.css('color', '#black');
    }     
    
    // set the selected label to be highlighted
    label.css('background', '#a6c9e2');
    label.css('color', 'black');     
    
    switch (label.html()) {
      case 'ALL':
        admin.assign.filterType = -1;   
        break; 
      case 'PENDING':
        admin.assign.filterType = 0;
        break;
      case 'ACTIVE':
        admin.assign.filterType = 1;
        break;
      case 'ARCHIVED':
        admin.assign.filterType = 2;
        break;      
    }    
          
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
  var totalPages = Math.ceil(admin.assign.total/admin.assign.pageSize);
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

  for ( var i = 0; i < admin.assign.assignments.length; i++) {
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
  //grid.autowidth = true;  
  grid.cellEdit = true;   
  
  admin.assign.initGridModels(grid);
  
  grid.afterInsertRow = function(rowid, rowdata, rowelem) {
    var entryId = admin.assign.getEntryId(rowid);

    var embedButton = '<input type="button" onclick=admin.assign.showEmbedCode("' + 
    entryId + '") value="embed" />';
    
    jQuery('#assignmentGrid').setCell(rowid, 'embed', embedButton);    
    
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
      return admin.assign.formatDate(date);
    }  
  });  
  
  grid.colNames.push('ID');
  grid.colModel.push({
    name: 'id', 
    index: 'id', 
    width: 70  
  });
  
  grid.colNames.push('Description');
  grid.colModel.push({
    name: 'description', 
    index: 'description', 
    width: 300,
    editable: true,
    edittype: 'text',
    editoptions: {rows:'3', cols: '30'},
    editrules: {required: true}
  });
  
  var optionFormattedString = (function(list) {    
    var ret = '';
    var categories = list.split(',');        
    for (var i=0; i < categories.length; i++) {
      var category = categories[i];
      ret += category + ':' + category;      
      if (i + 1 < categories.length) {
        ret += ';';
      }
    }
    return ret;
  })(admin.assign.ytCategories)
  
  grid.colNames.push('Category');
  grid.colModel.push( {
    name : 'category',
    index : 'category',
    width : 100,
    edittype : 'select',
    editable : true,
    editoptions : {
      value : optionFormattedString
    },
    sorttype : 'string',
    sortable: true
  });    
  
  grid.colNames.push('Status');
  grid.colModel.push( {
    name : 'status',
    index : 'status',
    width : 100,
    edittype : 'select',
    editable : true,
    editoptions : {
      value : 'ACTIVE:ACTIVE;PENDING:PENDING;ARCHIVED:ARCHIVED'
    },
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
  
  grid.colNames.push('Playlist ID');
  grid.colModel.push({
    name: 'playlistId', 
    index: 'playlistId', 
    width: 70,
    searchoptions: {sopt: ['eq', 'ne', 'cn', 'nc']},
    hidden: true
  });

  grid.colNames.push('Embed');
  grid.colModel.push( {
    name : 'embed',
    index : 'embed',
    width : 75,
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
  
  jQuery.ui.dialog.defaults.bgiframe = true;
  
  var code = [];
  code.push('<script type="text/javascript" src="' + admin.assign.getSelfUrl() 
      + '/js/yaw-embed.js" />\n');
  code.push('<script type="text/javascript">\n');
  code.push('window.onload = function() {\n');   
  code.push('  var yaw = new Yaw();\n');
  code.push('  yaw.setAssignmentId("' + entry.id + '");\n');
  code.push('  yaw.setCallToAction("callToActionId");\n');  
  code.push('  var containerWidth = 300;\n');
  code.push('  var containerHeight = 300;\n');
  code.push('  yaw.setYawContainer("yawContainer", containerWidth, containerHeight);\n');   
  code.push('  yaw.ready();\n');
  code.push('};\n');  
  code.push('</script>\n');
  code.push('<img src"callToActionImage.jpg" id="callToActionId" />\n');  
  code.push('<div id="yawContainer" />');  
  
  code = code.join('');
  code = code.replace(/\</g,'&lt;');
  code = code.replace(/\>/g,'&gt;');
  
  var textarea = jQuery('<textarea cols="80" cols="15"/>');
  textarea.css('font-size', '11px');
  textarea.css('color', 'black');
  textarea.css('border', '0px');
  textarea.html(code);
  
  var dialogOptions = {};
  dialogOptions.title = 'Embed code';
  dialogOptions.width = 400;
  dialogOptions.height = 270;  
   
  textarea.dialog(dialogOptions);

};

admin.assign.formatDate = function(date) {
  var year = admin.assign.padZero(date.getFullYear());
  var month = admin.assign.padZero(date.getMonth() + 1);
  var day = admin.assign.padZero(date.getDate());
  var hours = admin.assign.padZero(date.getHours());
  var minutes = admin.assign.padZero(date.getMinutes());
  var seconds = admin.assign.padZero(date.getSeconds());
  
  return year + '-' + month + '-' + day + ' ' + hours + ':' + minutes + ':' + seconds;
};

admin.assign.padZero = function(value) {
  value = value + '';
  if (value.length < 2) {
    return '0' + value;
  } else {
    return value;
  }
};

admin.assign.getAssignment = function(id) {
  var ret = null;

  for ( var i = 0; i < admin.assign.assignments.length; i++) {
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
  admin.assign.getAllAssignments( function(entries) {
    
    admin.assign.refreshGridUI(entries);
    
    var captionTitle = null;
    
    switch(admin.assign.filterType) {
      case -1:
        captionTitle = 'All Assignments';   
        break;
      case 0:
        captionTitle = 'Pending Assignments';
        break;
      case 1:
        captionTitle = 'Active Assignments';
        break;
      case 2:
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
  for ( var i = 0; i < entries.length; i++) {
    jqGrid.addRowData(i + 1, entries[i]);
  }
};

admin.assign.showAssignmentCreate = function() {

  var dialogOptions = {};
  dialogOptions.title = "Create New Assignment";
  dialogOptions.width = 300;
  dialogOptions.height = 300;
  
  jQuery.ui.dialog.defaults.bgiframe = true; 
  
  var div = jQuery('#assignmentCreate').clone();  
  
  var categorySelector = div.find('#assignmentCategories');
  
  var categories = admin.assign.ytCategories.split(',');  
  
  for (var i=0; i < categories.length; i++) {
    var category = categories[i];
    categorySelector.append('<option value="' + category + '">' + category + '</option>');    
  }  

  div.find('#createCancelButton').click(function() {
    div.dialog('destroy');
  });
  
  div.find('#createButton').click(function() {
    
    var newAssignment = {};
    newAssignment.description = div.find('#assignmentDescription').val();
    newAssignment.category = 
        div.find('#assignmentCategories').get(0).
        options[div.find('#assignmentCategories').attr('selectedIndex')].value;
    newAssignment.status = 
        div.find('#assignmentStatusType').get(0).
        options[div.find('#assignmentStatusType').attr('selectedIndex')].value;            
    
    var url = '/admin/NewAssignment';
    var ajaxCall = {};
    ajaxCall.type = 'POST';
    ajaxCall.url = url;
    ajaxCall.data = JSON.stringify(newAssignment);
    ajaxCall.cache = false;
    ajaxCall.processData = false;
    ajaxCall.success = function(res) {
      admin.assign.showLoading(false);
      admin.assign.pageIndex = 1;
      admin.assign.refreshGrid();      
    };

    admin.assign.showLoading(true, 'processing ...');
    jQuery.ajax(ajaxCall);    
    
    div.dialog('destroy');
    
  });  
  
  div.dialog(dialogOptions);    
};

admin.assign.statusToString = function(status) {

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

admin.assign.stringToStatus = function(str) {

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

admin.assign.getAllAssignments =function(callback) {
  var url = '/admin/GetAllAssignments?sortby=' + admin.assign.sortBy + '&sortorder=' +  
      admin.assign.sortOrder + '&pageindex=' + admin.assign.pageIndex + '&pagesize=' + 
      admin.assign.pageSize;

  if (admin.assign.filterType > -1) {
    url += '&filtertype=' + admin.assign.filterType;
  }  
  
  var ajaxCall = {};
  ajaxCall.cache = false;
  ajaxCall.type = 'GET';
  ajaxCall.url = url;
  ajaxCall.dataType = 'json';
  ajaxCall.success = function(result) {  
    admin.assign.total = result.total;
    var entries = result.entries                  
    admin.assign.assignments = entries.concat([]);
    admin.assign.showLoading(false);
    callback(entries);        
  };
  admin.assign.showLoading(true);
  jQuery.ajax(ajaxCall);
};

admin.assign.updateAssignment = function(entry) {
  var url = '/admin/UpdateAssignment';
  var ajaxCall = {};
  ajaxCall.type = 'POST';
  ajaxCall.url = url;
  ajaxCall.data = JSON.stringify(entry);
  ajaxCall.cache = false;
  ajaxCall.processData = false;
  ajaxCall.success = function(res) {
    admin.assign.showLoading(false);
  };

  admin.assign.showLoading(true, 'saving ...');
  jQuery.ajax(ajaxCall);

};

admin.assign.showLoading = function(status, text) {
  if (status) {
    text = text || 'loading ...';
    jQuery('#assignmentStatus').html(text).show();
  } else {
    jQuery('#assignmentStatus').html('').hide();
  }
};
