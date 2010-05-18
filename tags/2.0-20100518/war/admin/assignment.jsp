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

	<div id="assignmentFilters">
		<a href="javascript:void(0);" class="filter">ALL</a>&nbsp;&nbsp;
		<a href="javascript:void(0);" class="filter">ACTIVE</a>&nbsp;&nbsp;
		<a href="javascript:void(0);" class="filter">PENDING</a>&nbsp;&nbsp;		
		<a href="javascript:void(0);" class="filter">ARCHIVED</a>&nbsp;&nbsp;	
		<br>
		<br>		
	</div>	
	
	<div id="assignmentControlPanel">
			Filter: <input id="assignmentSearchText" type="text">
			&nbsp;&nbsp;&nbsp;
			<input id="assignmentRefreshGrid" value="Refresh" type="button"/>				
			<input id="assignmentPrevPage" value="<< Prev" type="button"/>
			<span id="assignmentPageIndex"></span>	
			<input id="assignmentNextPage" value="Next >>" type="button"/>
			&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
			<input id="assignmentCreateButton" value="New Assignment" type="button"/>	
	</div>
	<table id="assignmentGrid" class="scroll" cellpadding="0" cellspacing="0"></table>
	
	
	<div style="display: none;" id="assignmentCreateTemplate">		
	  <div>Category</div> 
	  <select id="assignmentCategories"></select>
	  <br><br>

	  <div>Status</div>
	  <select id="assignmentStatusType">
	  	<option value="ACTIVE" selected>ACTIVE</option>
	  	<option value="PENDING">PENDING</option>
	  	<option value="ARCHIVE">ARCHIVE</option>
	  </select>  
		<br><br>

		<div>YouTube Playlist Title</div>
		<input type="text" size="40" id="playlistTitle"/>
		<br><br>

		<div>YouTube Playlist Description</div>
		<textarea cols="40" rows="5" id="assignmentDescription"></textarea>		
		<br><br>
		
		<div>Assignment-Specific Initial Message (optional)</div>
    <textarea cols="40" rows="5" id="assignmentLoginInstruction"></textarea>   
    <br><br>
    
    <div>Assignment-Specific Post-Submit Message (optional)</div>
    <textarea cols="40" rows="5" id="assignmentPostSubmitMessage"></textarea>   
    <br><br>
		
		<input id="createButton" type="button" value="Create"/>
		<input id="createCancelButton" type="button" value="Cancel"/>
	</div>