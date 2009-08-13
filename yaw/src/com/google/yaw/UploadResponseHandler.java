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

		if (status.equals("200")) {

			UserSession userSession = UserSessionManager.getUserSession(req);
			String authSubToken = userSession.getAuthSubToken();
			String articleUrl = userSession.getArticleUrl();
			String assignmentId = userSession.getAssignmentId();
			String videoTitle = userSession.getVideoTitle();
			String videoDescription = userSession.getVideoDescription();
			String youTubeName = userSession.getYouTubeName();
			String tagList = userSession.getVideoTagList();
			
			log.warning("assignmentId is " + assignmentId);
			
			// create and persist VideoSubmission entry
			VideoSubmission submission = new VideoSubmission(
					assignmentId, articleUrl, videoId, videoTitle,
					videoDescription, tagList, youTubeName, authSubToken, Long.parseLong(assignmentId));

			Util.persistJdo(submission);

			try {
				JSONObject responseJsonObj = new JSONObject();
				responseJsonObj.put("videoId", videoId);
				responseJsonObj.put("status", status);
				resp.setContentType("text/javascript");
				resp.getWriter().println(responseJsonObj.toString());
			} catch (JSONException e) {
				e.printStackTrace();
				log.severe(e.getMessage());
				resp.setContentType("text/plain");
				resp.getWriter().println("fail");
			}
		} else {
			// what to do ...			
		}
	}
}