	<span id="configurationStatus"></span>
	<br><br>
	
  <label class="configureLabel">YouTube developer key: </label>
  <input class="configureInput" id="developerKey" type="text">
  <div class="clear" />
  
  <label class="configureLabel">YouTube client ID: </label>
  <input class="configureInput" id="clientId" type="text">
  <div class="clear" />
  
  <label class="configureLabel">YouTube account name: </label>
  <input class="configureInput" id="youTubeUsername" type="text">
  <div class="clear" />
  
  <label class="configureLabel">YouTube password: </label>
  <input class="configureInput" id="youTubePassword" type="password">
  <div class="clear" />
  
  <label class="configureLabel">Default YouTube Tag: </label>
  <input class="configureInput" id="defaultTag" type="text">
  <span class="tip">User-visible YouTube tag added to all approved submissions.</span>
  <div class="clear" />
  
  <label class="configureLabel">Link Back Text: </label>
  <input class="configureInput" id="linkBackText" type="text">
  <span class="tip">e.g.: "Submitted in response to article ARTICLE_URL." Added to approved videos' YouTube descriptions.</span>
  <div class="clear" />
  
  <label class="configureLabel">Moderation mode: </label>  
  <select class="configureInput" id="moderationMode">
  	<option value="0" selected>ON</option>
  	<option value="1">OFF</option>
  </select>
  <span class="tip">ON: Submissions must be moderated. OFF: Submissions are auto-approved.</span>
  <div class="clear" />
  
  <label class="configureLabel">Branding mode: </label>  
  <select class="configureInput" id="brandingMode">
  	<option value="0" selected>ON</option>
  	<option value="1">OFF</option>
  </select>
  <span class="tip">ON: Add branding to approved submissions' YouTube page. OFF: No branding.</span>
  <div class="clear" />
  
  <label class="configureLabel">Submission mode: </label>  
  <select class="configureInput" id="submissionMode">
  	<option value="0" selected>NEW OR EXISTING</option>
  	<option value="1">NEW ONLY</option>
  </select>
  <span class="tip">NEW OR EXISTING: Accept new and existing videos. NEW ONLY: Only accept new uploads.</span>
  <div class="clear" />

  <label class="configureLabel">Login instruction: </label>
  <textarea cols="50" rows="10" "configureInput" id="loginInstruction"></textarea>
	<div class="clear" />	
  
  <label class="configureLabel">&nbsp;</label>
  <input "configureInput" id="saveButton" type="button" value="save"/>
	<div class="clear" />