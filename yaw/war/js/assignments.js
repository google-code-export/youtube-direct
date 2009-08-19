jQuery(document).ready( function() {
  loadDataGrid();
});

function loadDataGrid() {
  var grid = {};
  
  grid.autoencode = true;
  grid.autowidth = true;
  grid.caption = 'Assignments';
  grid.cellEdit = true;
  grid.cellsubmit = 'remote';
  grid.cellurl = '/MutateAssignment';
  grid.datatype = 'json';
  grid.editurl = '/MutateAssignment';
  grid.gridview = true; // This prevents us from using the afterInsertRow event callback.
  grid.height = '400px';
  grid.pager = '#pager';
  grid.rownumbers = true;
  grid.rowNum = 5;
  grid.sortname = 'id';
  grid.sortorder = 'asc';
  grid.url = '/GetAllAssignments';
  grid.viewrecords = true;
  
  grid.colNames = [];
  grid.colModel = [];
  
  grid.colNames.push('Assignment ID');
  grid.colModel.push({
    name: 'id', 
    index: 'id', 
    width: 300,
    searchoptions: {sopt: ['eq', 'ne', 'cn', 'nc']},
  });
  
  grid.colNames.push('Description');
  grid.colModel.push({
    name: 'description', 
    index: 'description', 
    width: 300,
    editable: true,
    edittype: 'textarea',
    editoptions: {rows:'3', cols: '30'},
    editrules: {required: true},
    searchoptions: {sopt: ['eq', 'ne', 'cn', 'nc']},
  });
  
  grid.colNames.push('Category');
  grid.colModel.push({
    name: 'category', 
    index: 'category', 
    width: 100,
    editable: true,
    cellurl: '/MutateAssignment',
    edittype: 'select',
    editoptions: {
      dataUrl: '/GetOptionsHTML?type=category',
    },
    editrules: {required: true},
    stype: 'select',
    searchoptions: {
      sopt: ['eq', 'ne'],
      dataUrl: '/GetOptionsHTML?type=category',
    },
  });
  
  grid.colNames.push('Status');  
  grid.colModel.push({
    name: 'status', 
    index: 'status', 
    width: 80,      
    editable: true,
    cellurl: '/MutateAssignment',
    edittype: 'select',
    editoptions: {
      dataUrl: '/GetOptionsHTML?type=status',
    },
    editrules: {required: true},
    stype: 'select',
    searchoptions: {
      sopt: ['eq', 'ne'],
      dataUrl: '/GetOptionsHTML?type=status',
    },
  });
  
  grid.loadError = function(xhr, status, error) {
    showMessage('Could not load data: ' + xhr.statusText);
  };
  
  var jqGrid = jQuery('#datagrid').jqGrid(grid);
  
  editParams = {
    width: 350,
    closeAfterEdit: true,
    closeOnEscape: true,
  };
  addParams = {
    width: 350,
    closeAfterAdd: true,
    closeOnEscape: true,
  };
  deleteParams = {};
  searchParams = {
    closeAfterSearch: true,
  };
  viewParams = {};
  jqGrid.navGrid('#pager', {del: false}, editParams, addParams, deleteParams, searchParams, viewParams);
}

function showMessage(text) {
  if (typeof console != 'undefined') {
    console.log(text);
  }
  
  jQuery('#message').html(text);
}