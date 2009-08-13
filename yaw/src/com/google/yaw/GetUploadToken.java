package com.google.yaw;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gdata.data.media.mediarss.MediaCategory;
import com.google.gdata.data.media.mediarss.MediaDescription;
import com.google.gdata.data.media.mediarss.MediaKeywords;
import com.google.gdata.data.media.mediarss.MediaTitle;
import com.google.gdata.data.youtube.FormUploadToken;
import com.google.gdata.data.youtube.VideoEntry;
import com.google.gdata.data.youtube.YouTubeMediaGroup;
import com.google.gdata.data.youtube.YouTubeNamespace;
import com.google.gdata.util.ServiceException;
import com.google.yaw.model.Assignment;
import com.google.yaw.model.UserSession;
import com.google.yaw.model.Assignment.AssignmentStatus;

public class GetUploadToken extends HttpServlet {

	private static final Logger log = Logger.getLogger(GetUploadToken.class
			.getName());

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		String json = Util.getPostBody(req);

		try {
			JSONObject jsonObj = new JSONObject(json);

			String title = jsonObj.getString("title");
			String description = jsonObj.getString("description");
			String location = jsonObj.getString("location");

			JSONArray tagsArray = jsonObj.getJSONArray("tags");

			UserSession userSession = UserSessionManager.getUserSession(req);
			String authSubToken = userSession.getAuthSubToken();
			String articleUrl = userSession.getArticleUrl();
			String assignmentId = userSession.getAssignmentId();
			
			Assignment assignment = Util.getAssignmentByKey(assignmentId);
			if (assignment == null) {
			    throw new IllegalArgumentException(String.format(
			            "Could not find an assignment with id <%s>.", assignmentId));
			}
			AssignmentStatus status = assignment.getStatus();
			if (status != AssignmentStatus.ACTIVE) {
			    throw new IllegalArgumentException(String.format(
                        "Can't add a video to a non-ACTIVE assignment. " +
                        "Current status of assignment id <%s> is %s.", assignmentId, status));
			}
			
			userSession.setVideoTitle(title);
			userSession.setVideoDescription(description);
			userSession.setVideoLocation(location);
			userSession.setVideoTagList(tagsArray.toString());
			UserSessionManager.save(userSession);

			// Title length is 60 characters or 100 bytes.
			if (title.length() > 60) {
				title = title.substring(0, 59);
			}

			VideoEntry newEntry = new VideoEntry();
			YouTubeMediaGroup mg = newEntry.getOrCreateMediaGroup();

			mg.setTitle(new MediaTitle());
			mg.getTitle().setPlainTextContent(title);

			mg.addCategory(new MediaCategory(YouTubeNamespace.CATEGORY_SCHEME,
					assignment.getCategory()));

			log.warning("assignment category = " + assignment.getCategory());
			
			mg.setKeywords(new MediaKeywords());
			for (int i = 0; i < tagsArray.length(); i++) {
				String tag = tagsArray.getString(i).trim();
				mg.getKeywords().addKeyword(tag);
			}

			mg.setDescription(new MediaDescription());
			mg.getDescription().setPlainTextContent(
					"Uploaded in response to " + articleUrl + "\n\n"
							+ description);

			String defaultDeveloperTag = System
					.getProperty("com.google.yaw.DefaultDeveloperTag");
			if (defaultDeveloperTag != null && defaultDeveloperTag.length() > 0) {
				mg.addCategory(new MediaCategory(
						YouTubeNamespace.DEVELOPER_TAG_SCHEME,
						defaultDeveloperTag));
			}
			
			//mg.addCategory(new MediaCategory(YouTubeNamespace.DEVELOPER_TAG_SCHEME, assignmentId));

			YouTubeApiManager apiManager = new YouTubeApiManager();

			apiManager.setToken(authSubToken);

			try {

				FormUploadToken token = apiManager.getFormUploadToken(newEntry);

				String uploadToken = token.getToken();
				String uploadUrl = token.getUrl();

				JSONObject responseJsonObj = new JSONObject();
				responseJsonObj.put("uploadToken", uploadToken);
				responseJsonObj.put("uploadUrl", uploadUrl);

				resp.setContentType("text/javascript");
				resp.getWriter().println(responseJsonObj.toString());

			} catch (ServiceException e) {
				log.severe("Upload token failed: " + e.toString());
				e.printStackTrace();
				
				JSONObject responseJsonObj = new JSONObject();
				responseJsonObj.put("uploadToken", "null");
				responseJsonObj.put("uploadUrl", "null");
				responseJsonObj.put("error", e.toString());				

				resp.setContentType("text/javascript");
				resp.getWriter().println(responseJsonObj.toString());				
			}

		} catch (Exception e) {
			e.printStackTrace();
			log.warning(e.getMessage());
			resp.setContentType("text/plain");
			resp.getWriter().println(e.getMessage());
		}
	}

}