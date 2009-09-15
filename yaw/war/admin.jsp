<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ page import="com.google.appengine.api.users.User"%>
<%@ page import="com.google.appengine.api.users.UserService"%>
<%@ page import="com.google.appengine.api.users.UserServiceFactory"%>
<%@ page import="com.google.yaw.Util"%>
<%@ page import="com.google.yaw.model.AdminConfig"%>

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
	<li><a href="#moderate">Submissions</a></li>
	<li><a href="#assignments">Assignments</a></li>
	<li><a href="#configure">Configure</a></li>
</ul>

<div id="moderate"><br>
Filter: <input id="searchText" type="text"><br>
<br>
<div id="status" style="font-size: 11px; color: red;">&nbsp;</div>
<br>
<table id="datagrid" class="scroll" cellpadding="0" cellspacing="0"></table>
<div id="pager" class="scroll" style="text-align: center;"></div>
</div>

<div id="assignments"><iframe src="assignments.jsp" frameborder=0
	width="1200" height="600"></iframe>
</div>
	
<div id="configure">
	
	<div id="configureStatus" style="font-size: 11px; color: red;">&nbsp;</div>
	<br><br>
	
  <label class="configureLabel">YouTube developer key: </label>
  <input class="configureInput" id="developerKey" type="text">
  <div class="clear" />
  
  <label class="configureLabel">YouTube client ID: </label>
  <input class="configureInput" id="clientId" type="text">
  <div class="clear" />
  
  <label class="configureLabel">Moderation mode: </label>  
  <select class="configureInput" id="moderationMode">
  	<option value="0" selected>ON</option>
  	<option value="1">OFF</option>
  </select>
  <div class="clear" />
  
  <label class="configureLabel">Branding mode: </label>  
  <select class="configureInput" id="brandingMode">
  	<option value="0" selected>ON</option>
  	<option value="1">OFF</option>
  </select>
  <div class="clear" />
  
  <label class="configureLabel">Submission mode: </label>  
  <select class="configureInput" id="submissionMode">
  	<option value="0" selected>NEW OR EXISTING</option>
  	<option value="1">NEW ONLY</option>
  </select>
  <div class="clear" />

  <label class="configureLabel">Login instruction: </label>
  
  <textarea cols="50" rows="10" "configureInput" id="loginInstruction">
This website is using YouTube AnyWhere to receive video response submission.<br>  
<br>
Please login to your YouTube account to proceed. <br>
<br>
YouTube API ToS [ <a target="_blank" href="http://code.google.com/apis/youtube/terms.html">read</a> ]    
  </textarea>  
	
	<div class="clear" />	
  
  <label class="configureLabel">&nbsp;</label>
  <input "configureInput" id="saveButton" type="button" value="save"/>
	<div class="clear" />
	

	
</div>
</div>

</body>
</html>
