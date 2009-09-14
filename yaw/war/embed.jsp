<%@ page import="org.apache.commons.lang.StringEscapeUtils"%>
<%@ page import="com.google.yaw.Authenticator"%>
<%@ page import="com.google.yaw.Util"%>

<% 
	Authenticator authenticator = new Authenticator(request, response); 
%>

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

<div id="callToAction" align="center">
</div>

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

<span id="processing"></span>

<div id="submissionAsk">
	<div align="center">
	<br><br><br><br><br>
	<input id="uploadVideoButton" class="askButton" type="button" value="Upload a new video" />

	<%		
		if (!Util.isUploadOnly()) {		
	%> 	
	<br><br>
	<input id="existingVideoButton" class="askButton" type="button" value="Submit an existing video" />	
	<%
		}
	%> 	
	
	</div>
</div>

<div id="existingVideoMain">
	<div class="smallRed">* required</div>
	<br>
	<label class="required" for="videoUrl">Video URL:</label>	
	<br>
	<div><input class="inputBox" type="text" name="videoUrl" id="videoUrl" /></div>
	<span class="tip">Example: http://www.youtube.com/watch?v=A7y7NafWXeM</span><br>
	<br> 
	<label for="date">Date:</label>
	<br>
	<div><input class="inputBox" type="text" name="date" id="submitDate" /></div>
	<br>	
	<label for="location">Location:</label>
	<br>
	<div><input class="inputBox" type="text" name="location" id="submitLocation" /></div>
	<br>
	<label>Email me on approval: </label><input id="submitEmailAsk" type="checkbox" />
	<input class="emailInputBox" id="submitEmail" type="text" value=""/>
	<br>
	<br>
	<div align="center">
		<input id="submitButton" class="actionButton" type="button" value="Submit" />&nbsp;
		<input id="cancelSubmitButton" class="actionButton" type="button" value="Cancel" />
	</div>
</div>

<div id="uploaderMain">
	<div class="smallRed">* required</div>
	<br>	
	<label class="required" for="title">Video Title:</label>
	<br>
	<div><input class="inputBox" type="text" name="title" id="title" /></div>
	<br>
	<label class="required" for="description">Video Description:</label>
	<br>
	<div><textarea class="inputBox" name="description" id="description"></textarea></div>
	<br>
	<label class="required" for="tags">Tags:</label>&nbsp;<span class="small">(use "," to separate)</span>
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
	<label>Email me on approval: </label><input id="uploadEmailAsk" type="checkbox" />
	<input class="emailInputBox" id="uploadEmail" type="text" value=""/>
	<br><br> 
	<form id="uploadForm" action="" method="post" enctype="multipart/form-data"> 
	<label class="required" for="file">Select file: </label><input id="file" type="file" name="file" />
	<br>
	<br>
	<div align="center">		
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