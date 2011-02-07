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
  

  <div id="assignmentCreateTemplate" style="display: none;">
    <fieldset>
      <div class="clear">
        <label for="playlistTitle" class="configureLabel">Title:</label>
        <input class="configureInput" type="text" id="playlistTitle"/>
      </div>
      <div class="help">A title to use for the YouTube playlist and Picasa album. This will be user-visible!</div>
      
      <div class="clear">
        <label for="assignmentDescription" class="configureLabel">Description:</label>
        <textarea cols="50" rows="5" id="assignmentDescription"></textarea>
      </div>
      <div class="help">An internal description for the assignment. This is not user-visible.</div>
      
      <div class="clear">
        <label for="assignmentStatusType" class="configureLabel">Status:</label>
        <select class="configureInput" id="assignmentStatusType">
          <option value="ACTIVE">Active</option>
          <option value="PENDING">Pending</option>
          <option value="ARCHIVED">Archived</option>
        </select>
      </div>
      <div class="help">Active assignments can receive new submissions. Pending/Archived assignments cannot.</div>
      
      <div class="clear">
        <label for="assignmentCategories" class="configureLabel">YouTube Category:</label>
        <select class="configureInput" id="assignmentCategories"></select>
      </div>
      <div class="help">Newly-uploaded videos will be assigned this category in YouTube.</div>
      
      <div class="clear">
        <label for="assignmentLoginInstruction" class="configureLabel">Initial Message:</label>
        <textarea cols="50" rows="5" id="assignmentLoginInstruction"></textarea>
      </div>
      <div class="help">Optional: This text will be substituted in place of the <code>ASSIGNMENT_MESSAGE</code> macro in the pre-submission message.</div>
      
      <div class="clear">
        <label for="assignmentPostSubmitMessage" class="configureLabel">Initial Message:</label>
        <textarea cols="50" rows="5" id="assignmentPostSubmitMessage"></textarea>
      </div>
      <div class="help">Optional: This text will be substituted in place of the <code>ASSIGNMENT_MESSAGE</code> macro in the post-submission message.</div>
    </fieldset>
    
    <div class="actionButtons">
      <input id="createCancelButton" type="button" value="Cancel"/>
      <input id="createButton" type="button" value="Create"/>
    </div>
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
        <select class="configureInput" id="status">
          <option value="ACTIVE">Active</option>
          <option value="PENDING">Pending</option>
          <option value="ARCHIVED">Archived</option>
        </select>
      </div>
      <div class="help">Active assignments can receive new submissions. Pending/Archived assignments cannot.</div>
      
      <div class="clear">
        <label for="category" class="configureLabel">Assignment Category:</label>
        <select class="configureInput" id="category"></select>
      </div>
      <div class="help">Newly-uploaded videos will be assigned this category in YouTube.</div>
      
      <div class="clear">
        <label for="playlist" class="configureLabel">Assignment Playlist:</label>
        <select class="configureInput" id="playlist"></select>
      </div>
      <div class="help">When approved, videos will be added to this YouTube playlist.</div>
      
      <div class="clear">
        <label for="album" class="configureLabel">Assignment Album:</label>
        <select class="configureInput" id="album"></select>
      </div>
      <div class="help">When approved, pictures will be added to this Picasa album.</div>
    </fieldset>
    
    <div class="actionButtons">
      <input id="modifyCancelButton" type="button" value="Cancel"/>
      <input id="modifyButton" type="button" value="Modify"/>
    </div>
  </div>