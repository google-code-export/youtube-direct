var _ytLatestData = null;

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
  grid.editurl = '/MutateAssignment';
  
  grid.colNames = [];
  grid.colModel = [];
  
  grid.colNames.push('Assignment ID');
  grid.colModel.push({
    name: 'id', 
    index: 'id', 
    width: 300,
    sorttype: 'string',
  });
  
  grid.colNames.push('Description');
  grid.colModel.push({
    name: 'description', 
    index: 'description', 
    width: 300,
    editable: true,
    cellurl: '/MutateAssignment',
    edittype: 'textarea',
    editoptions: {rows:'3', cols: '30'},
    editrules: {required: true},
    sorttype: 'string',
  });
  
  grid.colNames.push('Category');
  grid.colModel.push({
    name: 'category', 
    index: 'category', 
    width: 100,
    editable: true,
    cellurl: '/MutateAssignment',
    edittype: 'select',
    editoptions: {value: window._ytCategories.join(';')},
    editrules: {required: true},
    sorttype: 'string',
  });
  
  grid.colNames.push('Status');  
  grid.colModel.push({
    name: 'status', 
    index: 'status', 
    width: 80,      
    editable: true,
    cellurl: '/MutateAssignment',
    edittype: 'select',
    editoptions: {value: window._ytAssignmentStatuses.join(';')},
    editrules: {required: true},
    sorttype: 'string',
  });
  
  grid.afterSaveCell = function(rowid, cellname, value, iRow, iCol) {
    var entry = jQuery('#datagrid').getRowData(rowid);
    entry.oper = 'edit';
    updateAssignment(entry);
  };

  grid.pager = jQuery('#pager');  
  var jqGrid = jQuery('#datagrid').jqGrid(grid);
  
  for(var i = 0; i < data.length; i++) {
    jqGrid.addRowData(data[i]['id'], data[i]);  
  }

  jqGrid.navGrid('#pager',
		  {edit: false, add: false, del: false, search: false, refresh: false})  
  .navButtonAdd('#pager', {
    buttonicon: 'ui-icon-plus',
    caption: 'Add Assignment',
    onClickButton: function() {
      jQuery('#datagrid').editGridRow('new', {
        width: 400,
        closeAfterAdd: true,
        reloadAfterSubmit: true, // This doesn't do anything...
      });
    },
  })
  .navButtonAdd('#pager', {
    buttonicon: 'ui-icon-refresh',
    caption: 'Refresh', 
    onClickButton: function() {
      getAllAssignments(function(data) {     
        jqGrid.clearGridData();

        for(var i = 0; i < data.length; i++) {
          jqGrid.addRowData(data[i]['id'], data[i]);  
        }
      });
    },
  });
}

function filterDescriptions() {
  descriptionFilter = jQuery('#descriptionFilter').val().toLowerCase();
  showMessage('Searching for "' + descriptionFilter + '" in assignment description...');
  
  jqGrid = jQuery('#datagrid');
  jqGrid.clearGridData();
  
  count = 0;
  for (var i = 0; i < _ytLatestData.length; i++) {
    description = _ytLatestData[i]['description'];
    if (description != null && description.toLowerCase().indexOf(descriptionFilter) != -1) {
      count++;
      jqGrid.addRowData(_ytLatestData[i]['id'], _ytLatestData[i]);
    }
  }
  
  showMessage('Found ' + count + ' article(s) whose description matches "' + descriptionFilter + '".');
}

function showMessage(text) {
  // console.log(text);
	jQuery('#message').html(text);
}

function getAllAssignments(callback) {
  var ajaxCall = {};
  ajaxCall.cache = false;
  ajaxCall.type = 'GET';
  ajaxCall.url = '/GetAllAssignments';
  ajaxCall.dataType = 'json';
  
  ajaxCall.success = function(data) {
    _ytLatestData = data;
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
  var ajaxCall = {};
  ajaxCall.type = 'POST';
  ajaxCall.url = '/MutateAssignment';
  ajaxCall.data = jQuery.param(entry);
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