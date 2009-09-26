// namespace protection against collision
var admin = admin || {};

jQuery(document).ready( function() {
	if (window.isLoggedIn) {
		jQuery('#tabs').tabs();
		admin.init();
	}
});

admin.init = function() {  
  admin.sub.init(); // from submission.js	
  admin.assign.init(); // from assignments.js
	admin.config.init(); //from configuration.js
};