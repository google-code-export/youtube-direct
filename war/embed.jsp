<%@ page import="com.google.inject.Guice"%>
<%@ page import="com.google.inject.Injector"%>
<%@ page import="com.google.inject.AbstractModule"%>
<%@ page import="org.apache.commons.lang.StringEscapeUtils"%>
<%@ page import="com.google.ytd.embed.Authenticator"%>
<%@ page import="com.google.ytd.embed.UserSessionManager"%>
<%@ page import="com.google.ytd.dao.UserAuthTokenDao"%>
<%@ page import="com.google.ytd.dao.UserAuthTokenDaoImpl"%>
<%@ page import="com.google.ytd.dao.AdminConfigDao"%>
<%@ page import="com.google.ytd.dao.AdminConfigDaoImpl"%>
<%@ page import="com.google.ytd.util.Util"%>
<%@ page import="com.google.ytd.model.AdminConfig"%>
<%@ page import="java.net.URLDecoder"%>
<%@ page import="javax.jdo.PersistenceManagerFactory"%>
<%@ page import="javax.servlet.http.HttpServletRequest"%>
<%@ page import="javax.servlet.http.HttpServletResponse"%><%@ page import="com.google.appengine.api.blobstore.BlobstoreServiceFactory" %>
<%@ page import="com.google.appengine.api.blobstore.BlobstoreService" %>


<%	
	final HttpServletRequest req = request;
	final HttpServletResponse resp = response;	

	Injector injector = Guice.createInjector(
	    new AbstractModule() {
	      protected void configure() {
		      bind(PersistenceManagerFactory.class).toInstance(
		          (PersistenceManagerFactory) getServletContext().getAttribute("pmf"));
		      bind(HttpServletRequest.class).toInstance(req);
		      bind(HttpServletResponse.class).toInstance(resp);
		      
	        bind(AdminConfigDao.class).to(AdminConfigDaoImpl.class);	 
		      bind(UserAuthTokenDao.class).to(UserAuthTokenDaoImpl.class);	    	 
	      }
	    });
	
	AdminConfigDao adminConfigDao = injector.getInstance(AdminConfigDao.class);
	AdminConfig adminConfig = adminConfigDao.getAdminConfig();
	Util util = injector.getInstance(Util.class);
	UserSessionManager userSessionManager = injector.getInstance(UserSessionManager.class);
	Authenticator authenticator = injector.getInstance(Authenticator.class);
	
	BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
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
<script type="text/javascript" src="/js/jsonrpc.js"></script>
<script type="text/javascript" src="/js/embed.js"></script>

</head>

<body>

<span id="youTubeLogo"><img src="icon.png"/></span>

<div align="center">
<div id="main">
<%
	if (authenticator.isLoggedIn()) {		
%> <%= authenticator.getUserSession().getMetaData("youTubeName") %> 
[ <a href="<%=authenticator.getLogOutUrl()%>">logout</a> ] 
<script type="text/javascript">
	window.isLoggedIn = true;
</script>
	
<%
	}
%>
<br>
<div id="message"></div> 
<br>

<div id="processing"></div>

<div align="center">
	<div id="loginInstruction" >			
		<%=URLDecoder.decode(adminConfig.getLoginInstruction(), "UTF-8") %>		
  </div>		
	<br>
		<%
			if (!authenticator.isLoggedIn()) {		
		%> 		
				
		<input onclick="javascript:top.location='<%=authenticator.getLogInUrl()%>';" 
					class="askButton" type="button" value="Login" />
		<%
			}	
		%> 	
</div>

<div id="submissionAsk">
	<div align="center">
	<br><br><br><br><br><br><br>
	<input id="uploadVideoButton" class="askButton" type="button" value="Upload a new video" />

	<%		
		if (!adminConfigDao.isUploadOnly()) {		
	%> 	
	<br><br>
	<input id="existingVideoButton" class="askButton" type="button" value="Submit an existing video" />	
	<%
		}
	%>
	
	<%    
    if (adminConfigDao.allowPhotoSubmission()) {   
  %>  
  <br><br>
  <input id="photoButton" class="askButton" type="button" value="Submit an existing photo" /> 
  <%
    }
  %>  
	
	</div>
</div>

<div align="center">
  <div id="postSubmitMessage">      
    <%=URLDecoder.decode(adminConfig.getPostSubmitMessage(), "UTF-8")%>   
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
	<br>
  <div id="youTubeTOS">
    By clicking 'Upload,' you certify that you own all rights to the content or that you are
    authorized by the owner to make the content publicly available on YouTube, and that it otherwise
    complies with the YouTube Terms of Service located at
    <a href="http://www.youtube.com/t/terms">http://www.youtube.com/t/terms</a>.
  </div>
</div>

<div id="photoMain">
  <form id="photoUploadForm" action="<%= blobstoreService.createUploadUrl("/SubmitPhoto") %>" method="post" enctype="multipart/form-data">
    <div class="smallRed">* required</div>
    <br>  
    <label class="required" for="title">Photo Title:</label>
    <br>
    <div>
      <input class="inputBox" type="text" name="title" id="title" />
    </div>
    <br>
    <label class="required" for="description">Photo Description:</label>
    <br>
    <div>
      <textarea class="inputBox" name="description" id="description"></textarea>
    </div>
    <br>
    <label for="date">Date:</label>
    <br>
    <div>
      <input class="inputBox" type="text" name="date" id="uploadDate" />
    </div>
    <br> 
    <label for="location">Location:</label>
    <br>
    <div>
      <input class="inputBox" type="text" name="location" id="uploadLocation" />
    </div>
    <br>
    <label class="required" for="uploadEmail">Your Email: </label>
    <div>
      <input class="inputBox" id="uploadEmail" name="uploadEmail" type="text" />
    </div>
    <br>
    <br>
    <label class="required" for="file1">Select File: </label>
    <div>
      <input id="file1" name="file1" type="file" />
    </div>
    <br>
    <br>
    <script type="text/javascript" src="http://api.recaptcha.net/challenge?k=6Le99goAAAAAABYfpbzUptcgAi0Q79s2ybGLsAxt"></script>
    <br>
    <div align="center">
      <input id="uploadButton" class="actionButton" type="submit" value="Upload" />
      <input id="cancelUploadButton" class="actionButton" type="button" value="Cancel" />
    </div>
  </form>
  <br>
  <div id="youTubeTOS">
    By clicking 'Upload,' you certify that you own all rights to the content or that you are
    authorized by the owner to make the content publicly available on this site.
  </div>
</div>
</div>
</div>
</body>

</html>