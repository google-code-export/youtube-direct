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

		<div>Assignment Title</div>
		<input type="text" size="40" id="playlistTitle"/>
		<br><br>

		<div>Assignment Description</div>
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
	
  <div id="assignmentEditTemplate" style="display: none;">
    <fieldset>
      <div class="clear">
        <label for="description" class="configureLabel">Description:</label>
        <span id="description"></span>
      </div>
      <div class="help">The assignment's description.</div>
      
      <div class="clear">
        <label for="status" class="configureLabel">Assignment Status:</label>
        <select id="status">
          <option value="ACTIVE">Active</option>
          <option value="PENDING">Pending</option>
          <option value="ARCHIVED">Archived</option>
        </select>
      </div>
      <div class="help">
        Active assignments can receive new submissions. Pending/Archived assignments cannot.
      </div>
      
      <div class="clear">
        <label for="category" class="configureLabel">Assignment Category:</label>
        <select id="category"></select>
      </div>
      <div class="help">
        Newly-uploaded videos will be assigned this category in YouTube.
      </div>
      
      <div class="clear">
        <label for="playlist" class="configureLabel">Assignment Playlist:</label>
        <select id="playlist"></select>
      </div>
      <div class="help">
        When approved, videos will be added to this YouTube playlist.
      </div>
      
      <div class="clear">
        <label for="album" class="configureLabel">Assignment Album:</label>
        <select id="album"></select>
      </div>
      <div class="help">
        When approved, pictures will be added to this Picasa album.
      </div>
    </fieldset>

    <input id="modifyButton" type="button" value="Modify"/>
    <input id="modifyCancelButton" type="button" value="Cancel"/>
  </div>