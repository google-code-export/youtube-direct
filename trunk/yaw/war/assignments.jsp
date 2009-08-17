<%@ page import="org.apache.commons.lang.StringEscapeUtils"%>
<%@ page import="com.google.yaw.YouTubeApiManager"%>
<%@ page import="com.google.yaw.model.Assignment"%>
<%@ page import="java.util.List"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" 
   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
  <head>
    <meta HTTP-EQUIV="CACHE-CONTROL" CONTENT="NO-CACHE">
    <title>YAW Assignments</title>

    <link type="text/css" href="/css/ext/themes/redmond/jquery-ui-1.7.2.custom.css" rel="stylesheet" />  
    <link type="text/css" href="/css/ext/ui.jqgrid.css" rel="stylesheet" />
  
    <script type="text/javascript" src="/js/ext/jquery-1.3.2.min.js"></script> 
    <script type="text/javascript" src="/js/ext/json2.js"></script>
    <script type="text/javascript" src="/js/ext/jquery-ui-1.7.2.custom.min.js"></script>
    <script type="text/javascript" src="/js/ext/i18n/grid.locale-en.js"></script> 
    <script type="text/javascript" src="/js/ext/jquery.jqGrid.min.js"></script>
    <script type="text/javascript" src="/js/assignments.js"></script>
    
    <script>
      window._ytAssignmentStatuses = [];
      window._ytCategories = [];
<%
  List<String> statuses = Assignment.getAssignmentStatusNames();
  for (String status: statuses) {
%>
      window._ytAssignmentStatuses.push('<%= status %>:<%= status %>');
<%
  }
  
  List<String> categories = YouTubeApiManager.getCategoryCodes();
  for (String category: categories) {
%>
      window._ytCategories.push('<%= category %>:<%= category %>');
<%
  }
%>
    </script>
  </head>

  <body>
    <div id="message"></div>
    <br/>
    <table id="datagrid"></table>
    <div id="pager"></div>
    <br/>
    <form action="javascript:filterDescriptions()">
      <label for="descriptionFilter">Search Descriptions:</label>
      <input type="text" id="descriptionFilter"></input>
      <input type="submit" value="Search"></input>
    </form>
  </body>
</html>