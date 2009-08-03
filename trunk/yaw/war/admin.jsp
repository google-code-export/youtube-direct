<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" 
   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
<head>
  <title>Admin</title>
  <script type="text/javascript" src="/js/ext/jquery-1.3.2.min.js"></script>
  <script type="text/javascript" src="/js/admin.js"></script>
</head>
<style>
body {
	margin: 10px;
	font-size: 11px;
	font-family: arial;
}
#videoDisplay {
	position: absolute;
	top: 0px;
	right: 0px;
}
</style>
<body>

<%
    UserService userService = UserServiceFactory.getUserService();
    User user = userService.getCurrentUser();
    if (user != null) {
%>
<p><%= user.getNickname() %> [ <a href="<%= userService.createLogoutURL(request.getRequestURI()) %>">logout</a> ]</p>

<script type="text/javascript">
window.isLoggedIn = true;
</script>

<%
    } else {
%>
<p> [ <a href="<%= userService.createLoginURL(request.getRequestURI()) %>">login</a> ]</p>

<script type="text/javascript">
window.isLoggedIn = false;
</script>

<%
    }
%>
  <span id="status" style="font-size: 11px; color: red;"></span>
  <br>
  <div id="videoList"></div>	
  <div id="videoDisplay"></div>
  </body>
</html>
