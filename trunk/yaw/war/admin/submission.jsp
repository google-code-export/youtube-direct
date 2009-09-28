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