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

	<div id="submissionFilters">
		<a href="javascript:void(0);" class="filter">ALL</a>&nbsp;&nbsp;
		<a href="javascript:void(0);" class="filter">UNREVIEWED</a>&nbsp;&nbsp;
		<a href="javascript:void(0);" class="filter">APPROVED</a>&nbsp;&nbsp;
		<a href="javascript:void(0);" class="filter">REJECTED</a>&nbsp;&nbsp;	
		<a href="javascript:void(0);" class="filter">SPAM</a>&nbsp;&nbsp;		
		<br>
		<br>		
	</div>
	
	<div id="submissionControlPanel">
			Filter: <input id="submissionSearchText" type="text">
			&nbsp;&nbsp;&nbsp;
			<input id="submissionRefreshGrid" value="Refresh" type="button"/>				
			<input id="submissionPrevPage" value="<< Prev" type="button"/>
			<span id="submissionPageIndex"></span>	
			<input id="submissionNextPage" value="Next >>" type="button"/>
	</div>
	<table id="submissionGrid" class="scroll" cellpadding="0" cellspacing="0"></table>
	
	<div style="height: 650px; display: none;" id="submissionDetailsTemplate" align="center">		  
	  <table cellspacing="10" cellspadding="10" style="width: 90%;">
	  	<tr valign="top">
	  		<td>
				  <b>Assignment ID:&nbsp;</b> <a href="#" id="changeAssignment">Change</a><div id="assignmentId"></div><br>
				  <b>Submitted on:</b><div id="created"></div><br>
				  <b>Submitted by:</b><div id="youTubeName"></div><br>
				  <b>Phone number:</b><div id="phoneNumber"></div><br>
				  <b>Video ID:</b><div id="videoId"></div><br>
				  <b>Video source:</b><div id="videoSource"></div><br>
				  <b>Video state:</b><div id="youTubeState"></div><br>
				  <b>Title:</b><div id="videoTitle"></div><br>
				  <b>Description: </b><div id="videoDescription"></div><br>
				  <b>Tags:</b><div id="videoTags"></div><br>
				  <b>Article URL:</b><div id="articleUrl"></div><br>
				  <b>Video taken on:</b><div id="videoDate"></div><br>
				  <b>Video location:</b><div id="videoLocation"></div><br>			  				    			
	  		</td>
	  		<td align="left">	  		  					
					<input id="download" type="button" value="Download Video"/>
					<input id="captions" type="button" value="Edit Captions"/>
	  			<div id="video"></div>
	  			<span>Moderation status: </span>
				  <select id="moderationStatus">
				  	<option value="UNREVIEW" selected>UNREVIEW</option>
				  	<option value="APPROVED">APPROVED</option>
				  	<option value="REJECTED">REJECTED</option>
				  	<option value="SPAM">SPAM</option>
				  </select>  
					<br><br><br>							  						   			
					<div><b>Admin notes:</b></div>
					<textarea cols="35" rows="5" id="adminNotes"></textarea><br>				
					<input id="saveAdminNotes" type="button" value="Save Notes"/>	
	  		</td>	  		
	  	</tr>
	  </table>	  	  		
	</div>
	
<div style="display: none" id="captionsTemplate">
  <div>
    <label>Choose a Caption Language: </label>
    <select id="languageSelect"></select>
    <a id="captionInfo" target="_blank" href="http://code.google.com/apis/youtube/2.0/developers_guide_protocol_captions.html#Supported_Caption_File_Formats">Info on Caption Format</a>
  </div>
  <textarea id="captionTrack" rows="20" cols="80"></textarea>
  <input id="saveCaption" type="button" value="Save Captions"></input>
</div>