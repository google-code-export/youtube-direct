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
  </head>

  <body>
<%
  String message = request.getParameter("message");
  if (message != null && message.length() > 0) {
%>
    <div><%= message %></div>
<%
  }
%>
    <div id="message"></div>
    <h3>Create a New Assignment</h3>
    <div id="createAssignment">
      <form id="createAssignmentForm" action="AssignmentHandler" method="post">
        <label for="description">Description:</label>
        <br>
        <textarea class="inputBox" name="description" id="description"></textarea>
        <br>
        <label for="assignmentStatus">Assignment Status:</label>
        <br>
        <script>window._ytAssignmentStatuses = [];</script>
        <select name="assignmentStatus" id="assignmentStatus">
<%
  List<String> statuses = Assignment.getAssignmentStatusNames();
  for (String status: statuses) {
%>
          <script>window._ytAssignmentStatuses.push('<%= status %>:<%= status %>');</script>
          <option value="<%= status %>"><%= status %></option>
<%
  }
%>
        </select>
        <br>
        <label for="category">YouTube Category:</label>
        <br>
        <script>window._ytCategories = [];</script>
        <select name="category" id="category">
<%
  List<String> categories = YouTubeApiManager.getCategoryCodes();
  for (String category: categories) {
%>
          <script>window._ytCategories.push('<%= category %>:<%= category %>');</script>
          <option value="<%= category %>"><%= category %></option>
<%
  }
%>
        </select>
        <br>
        <input type="submit" value="Create Assignment"/>
      </form>
    </div>
    <h3>Current Assignments</h3>
    <table id="datagrid" class="scroll" cellpadding="0" cellspacing="0"></table>
    <div id="pager" class="scroll" style="text-align:center;"></div>
  </body>
</html>