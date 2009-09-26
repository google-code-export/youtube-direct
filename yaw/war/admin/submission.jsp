	<span class="status" id="submissionStatus"></span>
	<br>
	<br>
	
	<div id="filters">
		<a id="allLabel" href="javascript:void(0);" class="filter">ALL</a>&nbsp;&nbsp;
		<a id="unreviewedLabel" href="javascript:void(0);" class="filter">UNREVIEWED</a>&nbsp;&nbsp;
		<a id="approvedLabel" href="javascript:void(0);" class="filter">APPROVED</a>&nbsp;&nbsp;
		<a id="rejectedLabel" href="javascript:void(0);" class="filter">REJECTED</a>&nbsp;&nbsp;	
		<a id="spamLabel" href="javascript:void(0);" class="filter">SPAM</a>&nbsp;&nbsp;		
		<br>
		<br>		
	</div>
	
	<br>
	<div id="controlPanel">
		<div id="navPanel">
			Filter: <input id="searchText" type="text">
			&nbsp;&nbsp;&nbsp;
			<input id="refreshGrid" value="Refresh" type="button"/>				
			<input id="prevPage" value="<< Prev" type="button"/>
			<span id="pageIndex"></span>	
			<input id="nextPage" value="Next >>" type="button"/>
		</div>
	</div>
	<table id="submissionGrid" class="scroll" cellpadding="0" cellspacing="0"></table>