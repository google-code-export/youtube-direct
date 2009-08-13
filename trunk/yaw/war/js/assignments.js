jQuery(document).ready( function() {
  //console.log(window.isLoggedIn);

  //if (window.isLoggedIn) {
    init();
  //}
});

function getAllAssignments() {
  var url = '/GetAllAssignments';
  var ajaxCall = {};
  ajaxCall.cache = false;
  ajaxCall.type = 'GET';
  ajaxCall.url = url;
  ajaxCall.dataType = 'json';
  
  ajaxCall.success = function(json) {
    console.log(json);
    displayAssignments(json);
    jQuery('#status').empty();
  };
  
  ajaxCall.error = function(XMLHttpRequest, textStatus, errorThrown) {
    console.log('XMLHttpRequest to ' + url + ' failed: ' + textStatus);
    jQuery('#status').html('Unable to retrieve assignments at this time.');
  };
  
  jQuery('#status').html('Loading assignments ...');
  jQuery.ajax(ajaxCall);  
}

function displayAssignments(assignments) {
  for (var i = 0; i < assignments.length; i++) {
    var assignment = assignments[i];
    var id = assignment.id;
    var description = assignment.description;
    var category = assignment.category;
    var status = assignment.status;
          
    var html = [];
    html.push('<div>id: ' + id + '</div>');
    html.push('<div>Description: ' + description + '</div>');
    html.push('<div>Category: ' + category + '</div>');
    html.push('<div>Status: ' + status + '</div>');
    html.push('<br><br>')
    
    jQuery('#currentAssignments').append(html.join(''));
  }
}

function init() {
  getAllAssignments();
}