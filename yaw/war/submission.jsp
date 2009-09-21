	<span id="submissionStatus"></span>
	<br>
	<br>
	
	<div id="filters">
		<a id="allLabel" href="javascript:void(0);" class="filter">ALL</a>&nbsp;&nbsp;
		<a id="unreviewedLabel" href="javascript:void(0);" class="filter">UNREVIEWED</a>&nbsp;&nbsp;
		<a id="approvedLabel" href="javascript:void(0);" class="filter">APPROVED</a>&nbsp;&nbsp;
		<a id="rejectedLabel" href="javascript:void(0);" class="filter">REJECTED</a>&nbsp;&nbsp;
	</div>
	
	<br>
	Filter: <input id="searchText" type="text">
	<br>
	<div id="controlPanel">
		<div id="navPanel">
			<input id="refreshGrid" value="refresh" type="button"/>	
			<input id="prevPage" value="<< prev" type="button"/>
			<span id="pageIndex"></span>	
			<input id="nextPage" value="next >>" type="button"/>
		</div>
	</div>
	<table id="submissionGrid" class="scroll" cellpadding="0" cellspacing="0"></table>