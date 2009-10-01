	<span class="status" id="submissionStatus"></span>
	<br>
	<br>
	
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
	
	<div style="display: none;" id="submissionDetailsTemplate" align="center">		  
	  <table cellspacing="10" cellspadding="10" style="width: 90%;">
	  	<tr valign="top">
	  		<td>
				  <b>Assignment ID:&nbsp;</b><div id="assignmentId"></div><br>
				  <b>Submitted on:</b><div id="submissionCreated"></div><br>
				  <b>Submitted by:</b><div id="submissionCreator"></div><br>
				  <b>Title:</b><div id="submissionTitle"></div><br>
				  <b>Description: </b><div id="submissionDescription"></div><br>
				  <b>Tags:</b><div id="submissionTags"></div><br>
				  <b>Article URL:</b><div id="submissionArticleUrl"></div><br>
				  <b>Video taken on:</b><div id="submissionVideoDate"></div><br>
				  <b>Video Location:</b><div id="submissionVideoLocation"></div><br>			  				    			
	  		</td>
	  		<td align="left">
	  			<div id="submissionVideo"></div>
	  			<span>Moderation status: </span>
				  <select id="submissionStatusType">
				  	<option value="UNREVIEW" selected>UNREVIEW</option>
				  	<option value="APPROVED">APPROVED</option>
				  	<option value="REJECTED">REJECTED</option>
				  	<option value="SPAM">SPAM</option>
				  </select>  
					<br><br><br>								  						   			
					<div><b>Notes:</b></div>
					<textarea cols="35" rows="5" id=""></textarea><br>				
					<input id="saveNotes" type="button" value="Save Notes"/>	
	  		</td>	  		
	  	</tr>
	  </table>	  	  		
	</div>