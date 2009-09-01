var _yt_selectedRowId = null;

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
    width: 275,
    searchoptions: {sopt: ['eq', 'ne', 'cn', 'nc']},
  });
  
  grid.colNames.push('Created On');
  grid.colModel.push({
    name: 'created', 
    index: 'created', 
    width: 200,
    search: false,
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
  
  grid.colNames.push('# of Submissions');  
  grid.colModel.push({
    name: 'submissionCount',
    index: 'submissionCount',
    width: 100,
    search: false,
    sortable: false,
  });
  
  grid.loadError = function(xhr, status, error) {
    showMessage('Could not load data: ' + xhr.statusText);
  };
  
  grid.beforeSelectRow = function(rowId) {
	_yt_selectedRowId = rowId;
	return true;
  };
  
  var jqGrid = jQuery('#datagrid').jqGrid(grid);
  
  var editParams = {
    width: 350,
    closeAfterEdit: true,
    closeOnEscape: true,
  };
  
  var addParams = {
    width: 350,
    closeAfterAdd: true,
    closeOnEscape: true,
  };
  
  var deleteParams = {};
  
  var searchParams = {
    closeAfterSearch: true,
  };
  
  var viewParams = {};
  
  var codeParams = {
	caption: 'Embed Code',
	onClickButton: generateEmbedCode,
	position: 'last',
  };
  
  jqGrid.navGrid('#pager', {del: false}, editParams, addParams, deleteParams, searchParams,
		  viewParams).navButtonAdd('#pager', codeParams);
}

function generateEmbedCode() {
  if (_yt_selectedRowId == null) {
	showMessage("Please select an assignment's row first.");
  } else {
	var embedCode = '<ul><li>Include a reference to <code>yaw-embed.js</code>:<br/>' +
		'<pre>&lt;script type="text/javascript" src="path/to/yaw-embed.js"&gt;&lt;/script&gt;</pre></li>' +
		'<li>Add a <code>window.onload</code> event to initialize a new <code>Yaw</code> object:<br/>' +
		'<pre>&lt;script type="text/javascript"&gt;\n' +
		'  window.onload = function() {\n' + 
		'    var yaw = new Yaw();\n' +
		'    yaw.setAssignmentId("' + _yt_selectedRowId + '");\n' +
		'    yaw.embed("yawContainer", 300, 500);\n' +
		'  };\n' +
		'&lt;/script&gt;</pre>';
	showMessage(embedCode);
  }
}

function showMessage(text) {
  if (typeof console != 'undefined') {
    console.log(text);
  }
  
  jQuery('#message').html(text);
}