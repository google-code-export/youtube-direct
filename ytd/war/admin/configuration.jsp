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

  <fieldset>
    <legend>YouTube API Settings</legend>
    <div class="clear">
      <label class="configureLabel">YouTube Account:</label>
      <span id="youTubeUsername">Unauthenticated</span>
      <input id="authenticateButton" type="button" value="Authenticate"/>
    </div>
    <div class="help">
      This account will contain the playlists for submitted videos.
    </div>
    <div class="clear">
      <label for="developerKey" class="configureLabel">YouTube Developer Key:</label>
      <input class="configureInput" id="developerKey" type="text"/>
    </div>
    <div class="help">
      Please <a target="_blank" href="http://code.google.com/apis/youtube/dashboard/gwt">register for a key</a> and then enter it above.
    </div>
    <div class="clear">
      <label for="privateKeyBytes" class="configureLabel">Private Key Certificate: </label>
      <textarea cols="50" rows="10" id="privateKeyBytes"></textarea>
    </div>
    <div class="help">
      Optional private key certificate. If you want to use secure AuthSub, follow <a target="_blank" href="http://code.google.com/p/youtube-direct/wiki/GettingStarted#Registering_your_website_for_AuthSub">these instructions</a> and enter the certificate above. Otherwise, leave blank.
    </div>
  </fieldset>
  <fieldset>
    <legend>Photo Settings</legend>
    <div class="clear">
      <label for="photoSubmissionEnabled" class="configureLabel">Photo Submissions:</label>
      <input type="checkbox" id="photoSubmissionEnabled">Enabled</input>
    </div>
    <div class="help">
      Check to enable photo submissions. You <b>must</b> <a target="_blank" href="http://code.google.com/appengine/docs/billing.html">enable billing</a> on your production App Engine instance <b>before</b> you enable photo submissions. YouTube Direct will not work otherwise.
    </div>
    <div id="photoSubmissionConfigDiv">
      <div class="clear">
        <label class="configureLabel">Picasa Account:</label>
        <span id="picasaUsername">Unauthenticated</span>
        <input id="picasaAuthenticateButton" type="button" value="Authenticate"/>
      </div>
      <div class="help">
        This account will contain the albums that hold photo submissions.
      </div>
      <div class="clear">
        <label for="maxPhotoSizeMb" class="configureLabel">Max. Photo Size (MB):</label>
        <select class="configureInput" id="maxPhotoSizeMb">
          <option value="1">1</option>
          <option value="3">3</option>
          <option value="5" selected="selected">5</option>
          <option value="10">10</option>
          <option value="20">20</option>
        </select>
      </div>
      <div class="help">
        Only individual photo submissions smaller than this size will be allowed.
      </div>
      <div class="clear">
        <label for="recaptchaPrivateKey" class="configureLabel">ReCaptcha Private Key: </label>
        <input class="configureInput" id="recaptchaPrivateKey" type="text"/>
      </div>
      <div class="help">
        Please register your *.appspot.com domain with <a target="_blank" href="http://www.google.com/recaptcha">reCAPTCHA</a> and enter the private key above.
      </div>
      <div class="clear">
        <label for="recaptchaPublicKey" class="configureLabel">ReCaptcha Public Key: </label>
        <input class="configureInput" id="recaptchaPublicKey" type="text"/>
      </div>
      <div class="help">
        Please register your *.appspot.com domain with <a target="_blank" href="http://www.google.com/recaptcha">reCAPTCHA</a> and enter the public key above.
      </div>
    </div>
  </fieldset>
  <fieldset>
    <legend>Video Branding Settings</legend>
    <div class="clear">
      <label for="brandingMode" class="configureLabel">Branding Mode:</label>
      <select class="configureInput" id="brandingMode">
        <option value="0" selected="selected">ON</option>
        <option value="1">OFF</option>
      </select>
    </div>
    <div class="help">
      If set to ON, the default YouTube tag and link back text will be added to all approved YouTube submissions. If set to OFF, the video submissions won't be modified when they're approved.
    </div>
    <div class="clear">
      <label for="defaultTag" class="configureLabel">Default YouTube Tag:</label>
      <input class="configureInput" id="defaultTag" type="text"/>
    </div>
    <div class="help">
      If the branding mode is ON, this tag will be added to all approved YouTube video submissions.
    </div>
    <div class="clear">
      <label for="linkBackText" class="configureLabel">Link Back Text:</label>
      <input class="configureInput" id="linkBackText" type="text"/>
    </div>
    <div class="help">
      If the branding mode is ON, this text will be prepended to the YouTube video's description. The macro <code>ARTICLE_URL</code> will be substituted for the URL of the page on which the video was submitted.
    </div>
  </fieldset>
  <fieldset>
    <legend>Moderation / Submission Settings</legend>
    <div class="clear">
      <label for="moderationMode" class="configureLabel">Moderation Mode:</label>
      <select class="configureInput" id="moderationMode">
        <option value="0" selected="selected">ON</option>
        <option value="1">OFF</option>
      </select>
    </div>
    <div class="help">
      If set to ON, then all submission are initially unreviewed, and need to be moderated. If set to OFF, all submissions are auto-approved.
    </div>
    <div class="clear">
      <label for="submissionMode" class="configureLabel">Submission Mode:</label>
      <select class="configureInput" id="submissionMode">
        <option value="0" selected="selected">NEW OR EXISTING</option>
        <option value="1">NEW ONLY</option>
      </select>
    </div>
    <div class="help">
      If set to NEW OR EXISTING, both new and existing YouTube submissions are allowed. If set to NEW ONLY, then existing YouTube videos cannot be submitted.
    </div>
    <div class="clear">
      <label for="newSubmissionAddress" class="configureLabel">Notification Address:</label>
      <input class="configureInput" id="newSubmissionAddress" type="text"/>
    </div>
    <div class="help">
      Comma-separated list of one or more addresses that will receive email for each new submission. Each address must be <a target="_blank" href="https://appengine.google.com/">configured</a> as an administrator for the App Engine instance.
    </div>
    <div class="clear">
      <label for="moderationEmail" class="configureLabel">Moderation Email:</label>
      <input type="checkbox" id="moderationEmail">Email User Upon Moderation</input>
    </div>
    <div class="help">
      If checked, the user will be sent mail when their submission is approved or rejected.
    </div>
    <div id="moderationEmailTextDiv">
      <div class="clear">
        <label for="fromAddress" class="configureLabel">Sender Email Address: </label>
        <input class="configureInput" id="fromAddress" type="text"/>
      </div>
      <div class="help">
        "From" address to use for user notification mails. The address must be <a target="_blank" href="https://appengine.google.com/">configured</a> as an administrator for the App Engine instance.
      </div>
      <div class="clear">
        <label for="approvalEmailText" class="configureLabel">Approval Email Text: </label>
        <textarea cols="50" rows="10" id="approvalEmailText"></textarea>
      </div>
      <div class="help">
        The body of the email sent to the user when a submission is approved. The following macros are supported: <code>ARTICLE_URL</code> (URL of original submission page) and <code>MEDIA_URL</code> (URL of YouTube video/Picasa photo).
      </div>
      <div class="clear">
        <label for="rejectionEmailText" class="configureLabel">Rejection Email Text: </label>
        <textarea cols="50" rows="10" id="rejectionEmailText"></textarea>
      </div>
      <div class="help">
        The body of the email sent to the user when a submission is rejected. The following macros are supported: <code>ARTICLE_URL</code> (URL of original submission page) and <code>MEDIA_URL</code> (URL of YouTube video/Picasa photo).
      </div>
    </div>
  </fieldset>
  <fieldset>
    <legend>Submissions Widget Settings</legend>
    <div class="clear">
      <label for="loginInstruction" class="configureLabel">Initial Message:</label>
      <textarea cols="50" rows="10" id="loginInstruction"></textarea>
    </div>
    <div class="help">
      Presented to user on the initial submission screen. HTML is okay. The macro <code>ASSIGNMENT_MESSAGE</code> will be replaced with an assignment-specific override, if configured for a given assignment.
    </div>
    <div class="clear">
      <label for="postSubmitMessage" class="configureLabel">Post-Submit Message:</label>
      <textarea cols="50" rows="10" id="postSubmitMessage"></textarea>
    </div>
    <div class="help">
      Presented to user after submission is complete. HTML is okay. The macro <code>ASSIGNMENT_MESSAGE</code> will be replaced with an assignment-specific override, if configured for a given assignment.
    </div>
  </fieldset>
  <div class="clear">
    <input id="saveButton" type="button" value="Save Changes"/>
  </div>