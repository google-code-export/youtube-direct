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
	
	<div id="photoControlPanel">
			Filter: <input id="photoSearchText" type="text">
			&nbsp;&nbsp;&nbsp;
			<input id="photoRefreshGrid" value="Refresh" type="button"/>				
			<input id="photoPrevPage" value="<< Prev" type="button"/>
			<span id="photoPageIndex"></span>	
			<input id="photoNextPage" value="Next >>" type="button"/>
	</div>
	<table id="photoGrid" class="scroll" cellpadding="0" cellspacing="0"></table>
	
	<div style="display: none;" id="photoDetailsTemplate" align="center">		  
	  <table border="0" cellspacing="0" cellspadding="0" style="width: 95%;">
	  	<tr valign="top">
	  		<td style="width: 300px;">
				  <b>Assignment ID:&nbsp;</b><div id="assignmentId"></div><br>
				  <b>Submitted on:</b><div id="created"></div><br>
				  <b>Title:</b><div id="title"></div><br>
				  <b>Description: </b><div id="description"></div><br>
				  <b>Article URL:</b><div id="articleUrl"></div><br>
				  <b>Photo location:</b><div id="location"></div><br>
				  <b>Phone number:</b><div id="phoneNumber"></div><br> 				    			
	  		</td>
	  		<td align="left">
	  		  <span><b>Apply action: </b></span>	  		 
	  		  <select id="photoAction">
            <option value="none" selected> ------ </option>
            <option value="approve">Mark as approved</option>
            <option value="reject">Mark as rejected</option>
            <option value="delete">Delete</option>            
          </select>
          <br>
          <br>
          &nbsp;&nbsp;<input id="selectAll" type="checkbox">&nbsp;<b><i>select all</i></b>
          <br>
          <br>          
	  			<div id="photos"></div>
					<br><br>	
					<b>Approved Photos: </b><span id="feedsDiv"></span>
          <br>
          <br>
          <br>				  						   			
					<div><b>Admin notes:</b></div>
					<textarea cols="35" rows="5" id="adminNotes"></textarea><br>				
					<input id="saveAdminNotes" type="button" value="Save Notes"/>	
	  		</td>	  		
	  	</tr>
	  </table>	  	  		
	</div>