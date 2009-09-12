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

<link type="text/css" href="/css/embed.css" rel="stylesheet" />

<script type="text/javascript" src="/js/ext/jquery-1.3.2.min.js"></script>
<script type="text/javascript" src="/js/ext/json2.js"></script>
<script type="text/javascript" src="/js/ext/jquery-ui-1.7.2.custom.min.js"></script>

<script type="text/javascript" src="/js/embed.js"></script>
</head>

<body>
<div align="center">
<div id="main">
<%
	if (!authenticator.isLoggedIn()) {		
%> 
<div>
<a href="javascript:top.location='<%=authenticator.getLogInUrl()%>';"><img src="icon.png" border="0"></a>
</div> 
<script type="text/javascript">
	window.isLoggedIn = false;
</script>
<%
	} else {
%> <%= authenticator.getUserSession().getMetaData("youTubeName") %> 
[ <a href="<%=authenticator.getLogOutUrl()%>">logout</a> ] 
<script type="text/javascript">
	window.isLoggedIn = true;
</script>

<br>
<div id="message">&nbsp;</div> 
<br>

<div id="submissionAsk">
	<div align="center">
	<br><br><br><br><br>
	<input id="uploadVideoButton" class="askButton" type="button" value="Upload a new video" />
	<br><br>
	<input id="existingVideoButton" class="askButton" type="button" value="Submit an existing video" />
	</div>
</div>

<span id="processing"></span>

<div id="existingVideoMain">
	<label for="videoId">Video ID:</label><br>
	<span class="tip">Tip: http://www.youtube.com/watch?v=<b>VIDEO_ID</b></span>
	<br>
	<div><input class="inputBox" type="text" name="videoId" id="videoId" /></div>
	<br> 
	<label for="date">Date:</label>
	<br>
	<div><input class="inputBox" type="text" name="date" id="submitDate" /></div>
	<br>	
	<label for="location">Location:</label>
	<br>
	<div><input class="inputBox" type="text" name="location" id="submitLocation" /></div>
	<br>
	Email me on approval: <input id="submitEmailAsk" type="checkbox" />
	<input id="submitEmail" type="text" value="" style="visibility: hidden"/>
	<br>
	<div align="center">
		<br>
		<input id="submitButton" class="actionButton" type="button" value="Submit" />&nbsp;
		<input id="cancelSubmitButton" class="actionButton" type="button" value="Cancel" />
	</div>
</div>

<div id="uploaderMain">
	<label for="title">Video Title:</label>
	<br>
	<div><input class="inputBox" type="text" name="title" id="title" /></div>
	<br>
	<label for="description">Video Description:</label>
	<br>
	<div><textarea class="inputBox" name="description" id="description"></textarea></div>
	<br>
	<label for="tags">Tags:</label>&nbsp;<span class="small">(use "," to separate)</span>
	<br>
	<div><input class="inputBox" type="text" name="tags" id="tags" /></div>
	<br>
	<label for="date">Date:</label>
	<br>
	<div><input class="inputBox" type="text" name="date" id="uploadDate" /></div>
	<br> 
	<label for="location">Location:</label>
	<br>
	<div><input class="inputBox" type="text" name="location" id="uploadLocation" /></div>
	<br>
	Email me on approval: <input id="uploadEmailAsk" type="checkbox" />
	<input id="uploadEmail" type="text" value="" style="visibility: hidden"/>
	<br>
	<br> 
	<form id="uploadForm" action="" method="post" enctype="multipart/form-data"> 
	Select file: <input id="file" type="file" name="file" />
	<br>
	<br>
	<div align="center">
		<br>
		<input id="token" type="hidden" name="token" value="">
		<input id="uploadButton" class="actionButton" type="submit" value="Upload" />&nbsp;
		<input id="cancelUploadButton" class="actionButton" type="button" value="Cancel" />
	</div>
	</form>
	
	<%
		}
	%>
</div>

</div>
</div>
</body>

</html>