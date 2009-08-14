<%@ page import="org.apache.commons.lang.StringEscapeUtils"%>
<%@ page import="com.google.yaw.Authenticator"%>

<% Authenticator authenticator = new Authenticator(request, response); %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" 
   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
<head>
<meta HTTP-EQUIV="CACHE-CONTROL" CONTENT="NO-CACHE">
<title>YAW</title>

<link type="text/css" href="/css/ext/themes/smoothness/jquery-ui-1.7.2.custom.css" rel="stylesheet" />

<link type="text/css" href="/css/yaw.css" rel="stylesheet" />

<script type="text/javascript" src="/js/ext/jquery-1.3.2.min.js"></script>
<script type="text/javascript" src="/js/ext/json2.js"></script>
<script type="text/javascript" src="/js/ext/jquery-ui-1.7.2.custom.min.js"></script>

<script type="text/javascript" src="/js/yaw.js"></script>
</head>

<body>
<div align="center">
<div id="main">
<%
	if (!authenticator.isLoggedIn()) {		
%> [ <a href="javascript:top.location='<%=authenticator.getLogInUrl()%>';">login</a>
] <%
	} else {
%> <%= authenticator.getUserSession().getYouTubeName() %> [ <a href="<%=authenticator.getLogOutUrl()%>">logout</a> ] <br>
<br>
<span id="message"></span> 
<br>
<div id="uploaderMain">
<form id="uploadForm" action="" method="post" enctype="multipart/form-data">
<label for="title">Title:</label>&nbsp;<span class="small">* required</span>
<br>
<input class="inputBox" type="text" name="title" id="title" />
<br>
<label for="description">Description:</label>&nbsp;<span class="small">* required</span>
<br>
<textarea class="inputBox" name="description" id="description"></textarea>
<br>
<label for="location">Location:</label>
<br>
<input class="inputBox" type="text" name="location" id="location" />
<br>
<label for="date">Date:</label>
<br>
<input class="inputBox" type="text" name="date" id="date" />
<br>
<label for="tags">Tags:</label>&nbsp;<span class="small">(use "," to separate)</span>
<br>
<input class="inputBox" type="text" name="tags" id="tags" />
<br>
<br> Choose file: 
<input id="file" type="file" name="file" />
<br>
<br>
<div align="center">
<br>
<input id="uploadButton" type="submit" value="Upload" />
<div id="uploading"></div>
</div>
<input id="token" type="hidden" name="token" value="">
</form>

<%
	}
%>
</div>
</div>
</div>
</body>

</html>