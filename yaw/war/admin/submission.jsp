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
				  <b>Submitted on:</b><div id="created"></div><br>
				  <b>Submitted by:</b><div id="youtubeName"></div><br>
				  <b>Video ID:</b><div id="videoId"></div><br>
				  <b>Title:</b><div id="videoTitle"></div><br>
				  <b>Description: </b><div id="videoDescription"></div><br>
				  <b>Tags:</b><div id="videoTags"></div><br>
				  <b>Article URL:</b><div id="articleUrl"></div><br>
				  <b>Video taken on:</b><div id="videoDate"></div><br>
				  <b>Video location:</b><div id="videoLocation"></div><br>			  				    			
	  		</td>
	  		<td align="left">
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