(function() {
  function showMessage(message, elementToHide, displaySeconds) {
    return showSomething(message, 'message', elementToHide, displaySeconds);
  };

  function showError(error, elementToHide, displaySeconds) {
    // Let's console.dir() exceptions if it's available and we're on a dev server (not port 443)
    if (window.location.port != 443 && typeof error != 'string' && typeof console != 'undefined' && typeof console.dir != 'undefined') {
      console.dir(error);
    }
    
    return showSomething(error, 'error', elementToHide, displaySeconds);
  };

  function showSomething(message, elementClass, elementToHide, displaySeconds) {
    displaySeconds = displaySeconds || 15;
    
    var wrapperElement = $('<p class="messageListWrapper">').prependTo('#messageList');
    $($.sprintf('<span class="%s">%s</span>', elementClass, message)).prependTo(wrapperElement);
    
    if (elementToHide) {
      $(elementToHide).hide();
    }
    
    if (typeof displaySeconds == 'number') {
      setTimeout(function() {
        wrapperElement.fadeOut('fast');
      }, displaySeconds * 1000);
    }
    
    return wrapperElement;
  };
  
  function bindInviteButtonHandler() {
    $('#invite').click(function() {
      var email = $('#new-email').val();
      var namespace = $('#new-namespace').val();
      var messageElement = showMessage($.sprintf('Inviting %s to admin %s.', email, namespace));

      $(this).attr('disabled', 'disabled');
      
      params = {
        namespace: namespace,
        email: email
      };

      jsonrpc.makeRequest('INVITE_EMAIL_TO_NAMESPACE', params, function(json) {
        if ('error' in json) {
          showError(json.error, messageElement);
        } else {
          loadNamespaceAdmins();
          showMessage($.sprintf('%s was invited to admin %s.', email, namespace), messageElement);
        }
      });
      
      $(this).removeAttr('disabled');
    });
  }
  
  function bindInviteLinkHandlers() {
    $('.invite').live('click', function() {
      $('#new-namespace').val($(this).data('namespace'));
      $('#new-email').focus();
    });
  }
  
  function bindRemoveLinkHandlers() {
    $('.remove').live('click', function() {
      var email = $(this).data('email');
      var namespace = $(this).data('namespace');
      
      var messageElement = showMessage($.sprintf('Removing %s\'s admin access to %s...',
          email, namespace));
      
      params = {
        namespace: namespace,
        email: email
      };

      jsonrpc.makeRequest('REMOVE_EMAIL_FROM_NAMESPACE', params, function(json) {
        if ('error' in json) {
          showError(json.error, messageElement);
        } else {
          loadNamespaceAdmins();
          showMessage($.sprintf('%s\'s admin access to %s was removed.', email, namespace),
              messageElement);
        }
      });
    });
  }
  
  function loadNamespaceAdmins() {
    var messageElement = showMessage('Loading instance information...');
    
    jsonrpc.makeRequest('GET_NAMESPACE_ADMINS', {}, function(json) {
      if ('error' in json) {
        showError(json.error, messageElement);
      } else {
        var html = [];
        $.each(json, function(namespace, entries) {
          if (!(entries instanceof Array)) {
            entries = [entries];
          }
          
          html.push($.sprintf('<div>%s | <a data-namespace="%s" href="#" class="invite">Invite New Admin</a><ul>', namespace, namespace));

          var sortedLis = [];
          $.each(entries, function(index, entry) {
            sortedLis.push($.sprintf('<li>%s %s| <a data-email="%s" data-namespace="%s" href="#" class="remove">Remove</a></li>',
                entry.email, entry.confirmed ? '' : '(unconfirmed) ', entry.email, namespace));
          });
          html.push(sortedLis.sort().join(''));
          
          html.push('</ul></div>');
        });
        
        $('#instances').html(html.join(''));
        showMessage('Instance information loaded.', messageElement);
      }
    });
  }
  
  $(function() {
    bindInviteButtonHandler();
    bindInviteLinkHandlers();
    bindRemoveLinkHandlers();
    loadNamespaceAdmins();
  });
})();