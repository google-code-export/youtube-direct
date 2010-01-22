/* Copyright (c) 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.ytd.mobile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gdata.data.youtube.VideoEntry;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.ytd.dao.AdminConfigDao;
import com.google.ytd.dao.AssignmentDao;
import com.google.ytd.dao.UserAuthTokenDao;
import com.google.ytd.model.AdminConfig;
import com.google.ytd.model.VideoSubmission;
import com.google.ytd.util.EmailUtil;
import com.google.ytd.util.PmfUtil;
import com.google.ytd.util.Util;
import com.google.ytd.youtube.YouTubeApiHelper;

/**
 * Servlet that handles mobile phone submissions, creating an appropriate
 * datastore entry for them.
 */
@Singleton
public class PersistMobileSubmission extends HttpServlet {
	private static final Logger log = Logger.getLogger(PersistMobileSubmission.class.getName());
	@Inject
	private Util util;
	@Inject
	private PmfUtil pmfUtil;
	@Inject
	private EmailUtil emailUtil;
	@Inject
	private YouTubeApiHelper youtubeApiProxy;
	@Inject
	private UserAuthTokenDao userAuthTokenDao;
	@Inject
	private AssignmentDao assignmentDao;
	@Inject
	private AdminConfigDao adminConfigDao;

	private String decode(String input) {
		// TODO: This should use a URL decode method from a library.
		input = input.replaceAll("%26", "&");
		return input.replaceAll("%3D|%3d", "=");
	}

	private Map<String, String> processPostData(String postDataString) {
		Map<String, String> submissionData = new HashMap<String, String>();
		// remove new lines
		postDataString = postDataString.replaceAll("\\n", "");
		String[] nameValuePairs = postDataString.split("&");
		for (String nameValuePair : nameValuePairs) {
			String[] tuple = nameValuePair.split("=");
			if (tuple.length >= 2) {
				String name = decode(tuple[0]);
				String value = decode(tuple[1]);
				log.info(name);
				log.info(value);
				submissionData.put(name, value);
			} else {
				submissionData = null;
			}
		}
		return submissionData;
	}

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		try {
			Map<String, String> submissionData = processPostData(util.getPostBody(req));
			if (submissionData == null) {
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "invalid post data format");
			}

			long assignmentId = -1;
			String videoId = null;
			String location = null;
			String date = null;
			String authSubToken = null;
			String email = null;

			assignmentId = submissionData.get("assignmentId") != null ? Long.parseLong(submissionData
					.get("assignmentId")) : -1;
			videoId = submissionData.get("videoId");
			location = submissionData.get("location");
			date = submissionData.get("date");
			authSubToken = submissionData.get("authSubToken");
			email = submissionData.get("email");

			if (assignmentId <= 0) {
				// get default mobile assignment ID
				assignmentId = assignmentDao.getDefaultMobileAssignmentId();
			}
			if (util.isNullOrEmpty(videoId)) {
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "missing videoId");
			}
			if (util.isNullOrEmpty(authSubToken)) {
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "missing authSubToken");
			}

			youtubeApiProxy.setToken(authSubToken);
			VideoEntry videoEntry = youtubeApiProxy.getUploadsVideoEntry(videoId);

			if (videoEntry == null) {
				resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
						"video id does not match the authsub token");
			} else {
				String youTubeName = videoEntry.getAuthors().get(0).getName();
				String title = videoEntry.getTitle().getPlainText();
				String description = videoEntry.getMediaGroup().getDescription().getPlainTextContent();
				List<String> tags = videoEntry.getMediaGroup().getKeywords().getKeywords();
				String sortedTags = util.sortedJoin(tags, ",");

				VideoSubmission submission = new VideoSubmission(assignmentId);
				submission.setVideoId(videoId);
				submission.setVideoTitle(title);
				submission.setVideoDescription(description);
				submission.setVideoTags(sortedTags);
				submission.setVideoLocation(location);
				submission.setVideoDate(date);
				submission.setYouTubeName(youTubeName);
				submission.setVideoSource(VideoSubmission.VideoSource.MOBILE_SUBMIT);
				submission.setNotifyEmail(email);

				userAuthTokenDao.setUserAuthToken(youTubeName, authSubToken);

				AdminConfig adminConfig = adminConfigDao.getAdminConfig();
				if (adminConfig.getModerationMode() == AdminConfig.ModerationModeType.NO_MOD.ordinal()) {
					// NO_MOD is set, auto approve all submission
					// TODO: This isn't enough, as the normal approval flow (adding the
					// branding, tags, emails,
					// etc.) isn't taking place.
					submission.setStatus(VideoSubmission.ModerationStatus.APPROVED);
				}
				pmfUtil.persistJdo(submission);
				emailUtil.sendNewSubmissionEmail(submission);

				resp.setContentType("text/plain");
				resp.getWriter().println("success");
			}
		} catch (IllegalArgumentException e) {
			log.log(Level.FINE, "", e);
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
		}
	}
}