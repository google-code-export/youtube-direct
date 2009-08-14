jQuery(document).ready( function() {
  //TODO: Ensure that users are logged in.
  //console.log(window.isLoggedIn);

  //if (window.isLoggedIn) {
  init();
  //}
});

function init() {
  getAllAssignments(initDataGrid);
}

function initDataGrid(data) { 
  var grid = {};
  grid.datatype = 'local';
  grid.height = 300;
  grid.multiselect = false;
  grid.caption = 'Assignments';
  grid.rowNum = -1;
  grid.cellsubmit = 'clientArray';
  grid.cellEdit = true;
  grid.autoWidth = true;
  grid.editurl = '/UpdateAssignment';
  
  grid.colNames = [];
  grid.colModel = [];
  
  grid.colNames.push('Assignment ID');
  grid.colModel.push({
    name: 'id', 
    index: 'id', 
    width: 240,
    sorttype: 'string',
  });
  
  grid.colNames.push('Description');
  grid.colModel.push({
    name: 'description', 
    index: 'description', 
    width: 300,
    editable: true,
    edittype: 'textarea',
    editoptions: {rows:'3', cols: '30'},
    sorttype: 'string',
  });
  
  grid.colNames.push('Category');
  grid.colModel.push({
    name: 'category', 
    index: 'category', 
    width: 100,
    editable: true,
    edittype: 'select',
    editoptions: {value: window._ytCategories.join(';')},
    sorttype: 'string',
  });
  
  grid.colNames.push('Assignment Status');  
  grid.colModel.push({
    name: 'status', 
    index: 'status', 
    width: 60,      
    edittype: 'select',
    editable: true,
    editoptions: {value: window._ytAssignmentStatuses.join(';')},
    sorttype: 'string',
  });
  
  grid.afterSaveCell = function(rowid, cellname, value, iRow, iCol) {
    var entry = jQuery('#datagrid').getRowData(rowid);
    updateAssignment(entry);
  };
  
  grid.pager = jQuery('#pager');  
  var jqGrid = jQuery('#datagrid').jqGrid(grid);
  
  for(var i = 0; i <= data.length; i++) {
    jqGrid.addRowData(i + 1, data[i]);  
  }

  jqGrid.navGrid('#pager',
		  {edit: true, add: true, del: false, search: false, refresh: false})
  .navButtonAdd('#pager', {
     caption: 'Refresh', 
     onClickButton: function() {
       getAllAssignments(function(data) {         
         jqGrid.clearGridData();

         for(var i = 0; i <= data.length; i++) {
           jqGrid.addRowData(i + 1, data[i]);  
         }
       });
     },
     position:"last"
  });
}

function showMessage(text) {
  console.log(text);
	jQuery('#message').html(text);
}

function getAllAssignments(callback) {
  var url = '/GetAllAssignments';
  var ajaxCall = {};
  ajaxCall.cache = false;
  ajaxCall.type = 'GET';
  ajaxCall.url = url;
  ajaxCall.dataType = 'json';
  
  ajaxCall.success = function(data) {
    showMessage('Assignments loaded successfully.');
    callback(data);
  };
  
  ajaxCall.error = function(xhr, textStatus, exception) {
    showMessage('Could not load assignments: ' + xhr.statusText);    
  }
  
  showMessage('Loading assignments...');
  jQuery.ajax(ajaxCall);
}

function updateAssignment(entry) {
  var url = '/UpdateAssignment';
  var ajaxCall = {};
  ajaxCall.type = 'POST';
  ajaxCall.url = url;
  ajaxCall.data = JSON.stringify(entry);
  ajaxCall.cache = false;
  ajaxCall.processData = false;
  ajaxCall.success = function() {
    showMessage('Assignment was updated successfully.');
  };
  
  ajaxCall.error = function(xhr, textStatus, exception) {
    showMessage('Could not update assignment: ' + xhr.statusText);    
  }
  
  showMessage('Updating assignment...');
  jQuery.ajax(ajaxCall);  
}