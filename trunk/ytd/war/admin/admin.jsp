<!--
Copyright 2009 Google Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
-->

<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ page import="com.google.appengine.api.users.User"%>
<%@ page import="com.google.appengine.api.users.UserService"%>
<%@ page import="com.google.appengine.api.users.UserServiceFactory"%>
<%@ page import="com.google.ytd.util.Util"%>
<%@ page import="com.google.ytd.model.AdminConfig"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" 
   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
<head>
<title>YouTube Direct Admin</title>

<link type="text/css" href="/css/ext/themes/redmond/jquery-ui-1.8.7.custom.css" rel="stylesheet" />
<link type="text/css" href="/css/ext/ui.jqgrid.css" rel="stylesheet" />
<link type="text/css" href="/css/admin.css" rel="stylesheet" />

<script type="text/javascript" src="/js/ext/jquery-1.4.4.min.js"></script>
<script type="text/javascript" src="/js/ext/json2.js"></script>
<script type="text/javascript" src="/js/ext/jquery-ui-1.8.7.custom.min.js"></script>
<script type="text/javascript" src="/js/ext/i18n/grid.locale-en.js"></script>
<script type="text/javascript" src="/js/ext/jquery.jqGrid.min.js"></script>
<script type="text/javascript" src="/js/ext/jquery.sprintf.js"></script>

<script type="text/javascript" src="/js/jsonrpc.js"></script>
<script type="text/javascript" src="/js/admin.js"></script>
<script type="text/javascript" src="/js/video_submission.js"></script>
<script type="text/javascript" src="/js/photo_submission.js"></script>
<script type="text/javascript" src="/js/assignment.js"></script>
<script type="text/javascript" src="/js/configuration.js"></script>
<script type="text/javascript" src='/_ah/channel/jsapi'></script>

</head>

<body>
<div id="messageContainer">
  <div id="messageList"></div>
</div>
<div align="center">
<div align="left"><h1>YouTube Direct Admin</h1>
<div id="authSection">
  <%
    UserService userService = UserServiceFactory.getUserService();
    User user = userService.getCurrentUser();
    if (user != null) {
  %>
  <p><b><%=user.getNickname()%></b> [ <a href="<%=userService.createLogoutURL(request.getRequestURI())%>">logout</a> ]</p>
  <script type="text/javascript">
  window.isLoggedIn = true;
  </script> <%
     } else {
   %>
  <p>[ <a href="<%=userService.createLoginURL(request.getRequestURI())%>">login</a> ]</p>
  <script type="text/javascript">
  window.isLoggedIn = false;
  </script> <%
     }
   %>
</div>
</div>
<div id="tabs">
	<ul>
		<li><a href="#videoSubmission">Video Submission</a></li>	
		<li><a href="#photoSubmission">Photo Submission</a></li>	
		<li><a href="#assignment">Assignment</a></li>
		<li><a href="#configuration">Configuration</a></li>
	</ul>
	
	<div id="videoSubmission">
		<%@include file="video_submission.jsp" %> 
	</div>
	
  <div id="photoSubmission">
    <%@include file="photo_submission.jsp" %> 
  </div>	
	
	<div id="assignment">
		<%@include file="assignment.jsp" %> 
	</div>
		
	<div id="configuration">
		<%@include file="configuration.jsp" %> 
	</div>
</div>
</div>
</body>
</html>