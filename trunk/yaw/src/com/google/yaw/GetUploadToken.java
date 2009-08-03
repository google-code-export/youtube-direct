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
import com.google.yaw.model.UserSession;

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
			String articleId = userSession.getArticleId();
			String partnerId = userSession.getPartnerId();
			
			userSession.setVideoTitle(title);
			userSession.setVideoDescription(description);
			userSession.setVideoLocation(location);
			userSession.setVideoTagList(tagsArray.toString());
			UserSessionManager.save(userSession);

			if (title.length() > 100) {
				title = title.substring(0, 99);
			}

			VideoEntry newEntry = new VideoEntry();
			YouTubeMediaGroup mg = newEntry.getOrCreateMediaGroup();

			mg.setTitle(new MediaTitle());
			mg.getTitle().setPlainTextContent(title);

			mg.addCategory(new MediaCategory(YouTubeNamespace.CATEGORY_SCHEME,
					"News"));

			for (int i = 0; i < tagsArray.length(); i++) {
				String tag = tagsArray.getString(i).trim();
				mg.setKeywords(new MediaKeywords());
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

			mg.addCategory(new MediaCategory(
					YouTubeNamespace.DEVELOPER_TAG_SCHEME, articleId));

			YouTubeApiManager apiManager = new YouTubeApiManager();

			apiManager.setToken(authSubToken);

			try {
				// This will make a POST request and obtain an upload token and
				// URL
				// that
				// can be used to submit a new video with the given metadata.

				FormUploadToken token = apiManager.getFormUploadToken(newEntry);

				String uploadToken = token.getToken();
				String uploadUrl = token.getUrl();

				JSONObject responseJsonObj = new JSONObject();
				responseJsonObj.put("uploadToken", uploadToken);
				responseJsonObj.put("uploadUrl", uploadUrl);

				resp.setContentType("text/json");
				resp.getWriter().println(responseJsonObj.toString());

			} catch (ServiceException e) {
				log.severe("Upload token failed: " + e.toString());
				e.printStackTrace();
				
				JSONObject responseJsonObj = new JSONObject();
				responseJsonObj.put("uploadToken", "null");
				responseJsonObj.put("uploadUrl", "null");
				responseJsonObj.put("error", e.toString());				

				resp.setContentType("text/json");
				resp.getWriter().println(responseJsonObj.toString());				
			}

		} catch (JSONException e) {
			e.printStackTrace();
			log.warning(e.getMessage());
			resp.setContentType("text/plain");
			resp.getWriter().println(e.getMessage());
		}
	}

}