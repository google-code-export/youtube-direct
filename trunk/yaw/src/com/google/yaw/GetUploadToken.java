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

	        // Only check for required parameters 'title' and 'description'.
	        if (Util.isNullOrEmpty(title)) {
	            throw new IllegalArgumentException("'title' parameter is null or empty.");
	        }
	        if (Util.isNullOrEmpty(description)) {
	            throw new IllegalArgumentException("'description' parameter is null or empty.");
	        }

	        UserSession userSession = UserSessionManager.getUserSession(req);
	        if (userSession == null) {
	            //TODO: Throw a better Exception class here.
	            throw new IllegalArgumentException("No user session found.");
	        }
	        String authSubToken = userSession.getAuthSubToken();
	        String articleUrl = userSession.getArticleUrl();
	        String assignmentId = userSession.getassignmentId();

	        Assignment assignment = Util.getAssignmentByKey(assignmentId);
	        if (assignment == null) {
	            throw new IllegalArgumentException(String.format(
	                            "Could not find an assignment with id '%s'.", assignmentId));
	        }
	        AssignmentStatus status = assignment.getStatus();
	        if (status != AssignmentStatus.ACTIVE) {
	            throw new IllegalArgumentException(String.format(
	                            "Can't add a video to a non-ACTIVE assignment. " +
	                            "Current status of assignment id '%s' is '%s'.",
	                            assignmentId, status));
	        }

	        userSession.setVideoTitle(title);
	        userSession.setVideoDescription(description);
	        userSession.setVideoLocation(location);
	        userSession.setVideoTagList(tagsArray.toString());
	        UserSessionManager.save(userSession);

	        // Max title length is 60 characters or 100 bytes.
	        if (title.length() > 60) {
	            title = title.substring(0, 59);
	        }

	        VideoEntry newEntry = new VideoEntry();
	        YouTubeMediaGroup mg = newEntry.getOrCreateMediaGroup();

	        mg.setTitle(new MediaTitle());
	        mg.getTitle().setPlainTextContent(title);

	        mg.addCategory(new MediaCategory(YouTubeNamespace.CATEGORY_SCHEME,
	                        assignment.getCategory()));

	        mg.setKeywords(new MediaKeywords());
	        for (int i = 0; i < tagsArray.length(); i++) {
	            String tag = tagsArray.getString(i).trim();
	            mg.getKeywords().addKeyword(tag);
	        }

	        mg.setDescription(new MediaDescription());
	        mg.getDescription().setPlainTextContent(String.format(
	                        "Uploaded in response to %s\n\n%s", articleUrl, description));

	        String defaultDeveloperTag = System.getProperty("com.google.yaw.DefaultDeveloperTag");
	        if (!Util.isNullOrEmpty(defaultDeveloperTag)) {
	            mg.addCategory(new MediaCategory(YouTubeNamespace.DEVELOPER_TAG_SCHEME,
	                            defaultDeveloperTag));
	        }

	        //TODO: This might be longer than the max allowed developer tag.
	        mg.addCategory(new MediaCategory(YouTubeNamespace.DEVELOPER_TAG_SCHEME, assignmentId));

	        YouTubeApiManager apiManager = new YouTubeApiManager();
	        apiManager.setToken(authSubToken);

	        FormUploadToken token = apiManager.getFormUploadToken(newEntry);

	        String uploadToken = token.getToken();
	        String uploadUrl = token.getUrl();

	        JSONObject responseJsonObj = new JSONObject();
	        responseJsonObj.put("uploadToken", uploadToken);
	        responseJsonObj.put("uploadUrl", uploadUrl);

	        resp.setContentType("text/javascript");
	        resp.getWriter().println(responseJsonObj.toString());
	    } catch (IllegalArgumentException e) {
	        log.finer(e.toString());
	        resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
	    } catch (JSONException e) {
	        log.warning(e.toString());
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
	    } catch (ServiceException e) {
	        log.warning(e.toString());
	        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
	    }
	}
}