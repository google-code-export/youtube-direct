<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ page import="com.google.appengine.api.users.User"%>
<%@ page import="com.google.appengine.api.users.UserService"%>
<%@ page import="com.google.appengine.api.users.UserServiceFactory"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" 
   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
<head>
<title>Admin</title>


<link type="text/css"
	href="/css/ext/themes/redmond/jquery-ui-1.7.2.custom.css"
	rel="stylesheet" />
<link type="text/css" href="/css/ext/ui.jqgrid.css" rel="stylesheet" />
<link type="text/css" href="/css/admin.css" rel="stylesheet" />

<script type="text/javascript" src="/js/ext/jquery-1.3.2.min.js"></script>
<script type="text/javascript" src="/js/ext/json2.js"></script>
<script type="text/javascript"
	src="/js/ext/jquery-ui-1.7.2.custom.min.js"></script>
<script type="text/javascript" src="/js/ext/i18n/grid.locale-en.js"></script>
<script type="text/javascript" src="/js/ext/jquery.jqGrid.min.js"></script>
<script type="text/javascript" src="/js/admin.js"></script>


</head>

<body>
<div id="authSection">
<%
	UserService userService = UserServiceFactory.getUserService();
	User user = userService.getCurrentUser();
	if (user != null) {
%>
<p><b><%=user.getNickname()%></b> [ <a
	href="<%=userService.createLogoutURL(request.getRequestURI())%>">logout</a>
]</p>
<script type="text/javascript">
window.isLoggedIn = true;
</script> <%
 	} else {
 %>
<p>[ <a
	href="<%=userService.createLoginURL(request.getRequestURI())%>">login</a>
]</p>
<script type="text/javascript">
window.isLoggedIn = false;
</script> <%
 	}
 %>
</div>

<div id="tabs">
<ul>
	<li><a href="#assignments">Assignments</a></li>
	<li><a href="#moderate">Video Submissions</a></li>
	<li><a href="#configure">Configure</a></li>
</ul>
<div id="assignments"><iframe src="assignments.jsp" frameborder=0
	width="1000" height="600"></iframe></div>
<div id="moderate"><br>
Filter: <input id="searchText" type="text"><br>
<br>
<div id="status" style="font-size: 11px; color: red;">&nbsp;</div>
<br>
<table id="datagrid" class="scroll" cellpadding="0" cellspacing="0"></table>
<div id="pager" class="scroll" style="text-align: center;"></div>
</div>
<div id="configure">
<p></p>
</div>
</div>

</body>
</html>
