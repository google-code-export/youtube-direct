package com.google.yaw.admin;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.yaw.Util;
import com.google.yaw.model.VideoSubmission;

public class GetAllSubmissions extends HttpServlet {

	private static final Logger log = Logger.getLogger(GetAllSubmissions.class
			.getName());

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		PersistenceManagerFactory pmf = Util.getPersistenceManagerFactory();
		PersistenceManager pm = pmf.getPersistenceManager();

		Query query = pm.newQuery(VideoSubmission.class);
		List<VideoSubmission> list = (List<VideoSubmission>) query.execute();

		JSONArray jsonArray = new JSONArray();

		for (VideoSubmission entry : list) {
			String videoId = entry.getVideoId();
			String assignmentId = entry.getAssignmentId();
			String articleUrl = entry.getArticleUrl();
			String title = entry.getVideoTitle();
			String description = entry.getVideoDescription();
			String tagList = entry.getVideoTagList();
			String uploader = entry.getYouTubeName();
			long updated = entry.getUpdated().getTime();
			int status = entry.getStatus().ordinal();

			try {
				JSONObject jsonObj = new JSONObject();
				jsonObj.put("videoId", videoId);
				jsonObj.put("articleUrl", articleUrl);
				jsonObj.put("assignmentId", assignmentId);
				jsonObj.put("title", title);
				jsonObj.put("description", description);
				jsonObj.put("tags", tagList);
				jsonObj.put("uploader", uploader);
				jsonObj.put("updated", updated);
				jsonObj.put("status", status);

				jsonArray.put(jsonObj);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		pm.close();

		resp.setContentType("text/javascript");
		resp.getWriter().println(jsonArray);

	}
}
