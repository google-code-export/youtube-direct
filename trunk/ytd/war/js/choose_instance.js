(function() {
  //Given a string, HTML escape it.
  function escapeHTML(s) {
    return s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
  }
  
  $(function() {
    var instances = $(document).getUrlParam('instances');
    $.each(instances.split(','), function(index, instance) {
      var escapedInstance = escapeHTML(instance);
      $('#instance-list').append($.sprintf('<li><a href="/admin?ns=%s">%s</a></li>',
          escapedInstance, escapedInstance));
    });
  });
})();