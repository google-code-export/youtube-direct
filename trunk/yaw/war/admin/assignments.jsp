	<span class="status" id="assignmentStatus"></span>
	<br>	
	<br>
	<div id="assignmentControlPanel">
		<div id="assignmentNavPanel">
			<input id="assignmentCreateButton" value="New Assignment" type="button"/>	
			&nbsp;&nbsp;&nbsp;
			<input id="assignmentRefreshGrid" value="Refresh" type="button"/>				
			<input id="assignmentPrevPage" value="<< Prev" type="button"/>
			<span id="assignmentPageIndex"></span>	
			<input id="assignmentNextPage" value="Next >>" type="button"/>
		</div>
	</div>
	<table id="assignmentGrid" class="scroll" cellpadding="0" cellspacing="0"></table>
	
	
	<div style="display: none;" id="assignmentCreate">	
		
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
			  		
		<div>Description</div>
		<textarea cols="40" rows="5" id="assignmentDescription"></textarea>		
		<br><br>		
		
		<input id="createButton" type="button" value="create"/>
		
	</div>