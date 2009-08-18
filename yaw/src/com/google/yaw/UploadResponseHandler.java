package com.google.yaw;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.yaw.model.Assignment;
import com.google.yaw.model.UserSession;
import com.google.yaw.model.VideoSubmission;

public class UploadResponseHandler extends HttpServlet {

	private static final Logger log = Logger
			.getLogger(UploadResponseHandler.class.getName());

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		String videoId = req.getParameter("id");
		String status = req.getParameter("status");

		UserSession userSession = UserSessionManager.getUserSession(req);
		
		if (status.equals("200")) {
			String authSubToken = userSession.getAuthSubToken();
			String articleUrl = userSession.getArticleUrl();
			String assignmentId = userSession.getAssignmentId();
			String videoTitle = userSession.getVideoTitle();
			String videoDescription = userSession.getVideoDescription();
			String uploader = userSession.getYouTubeName();
			String email = userSession.getEmail();
			String videoTags = userSession.getVideoTagList();
			
			log.fine(String.format("Attempting to persist VideoSubmission with YouTube id '%s' " +
					"for assignment id '%s'...", videoId, assignmentId));
			VideoSubmission submission = new VideoSubmission(
					assignmentId, articleUrl, videoId, videoTitle,
					videoDescription, videoTags, uploader, authSubToken);
			
			submission.setEmail(email);
			
			Util.persistJdo(submission);
	        log.fine("...VideoSubmission persisted.");

			try {
				JSONObject responseJsonObj = new JSONObject();
				responseJsonObj.put("videoId", videoId);
				responseJsonObj.put("status", status);				
				resp.setContentType("text/javascript");
				resp.getWriter().println(responseJsonObj.toString());
			} catch (JSONException e) {
	            log.warning(e.toString());
	            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
			}
		} else {
		    String code = req.getParameter("code");
		    log.warning(String.format("Upload request for user with session id '%s' failed with " +
		    		"status '%s' and code '%s'.", userSession.getId(), status, code));
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, code);
		}
	}
}