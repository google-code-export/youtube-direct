jQuery(document).ready( function() {
  //TODO: Ensure that users are logged in.
  //console.log(window.isLoggedIn);

  //if (window.isLoggedIn) {
    init();
  //}
});

function init() {
  jQuery("#assignments").jqGrid({
    url:'/GetAllAssignments',
    datatype: 'json',
    mtype: 'GET',
    colNames:['Assignment ID', 'Description', 'Category', 'Status'],
    colModel: [
      {name: 'assignmentId', index: 'assignmentId', width: 240}, 
      {name: 'description', index: 'description', width: 300}, 
      {name: 'category', index: 'category', width: 80}, 
      {name:'status', index:'status', width: 60}, 
    ],
    pager: '#pager',
    rowNum: -1,
    sortname: 'assignmentId',
    sortorder: 'asc',
    viewrecords: true,
    caption: 'Assignments'
  }); 
}