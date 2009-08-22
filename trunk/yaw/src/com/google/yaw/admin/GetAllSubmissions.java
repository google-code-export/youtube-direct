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

import com.google.yaw.Util;
import com.google.yaw.model.VideoSubmission;

public class GetAllSubmissions extends HttpServlet {

	private static final Logger log = Logger.getLogger(GetAllSubmissions.class
			.getName());

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		PersistenceManagerFactory pmf = Util.getPersistenceManagerFactory();
		PersistenceManager pm = pmf.getPersistenceManager();

		Query query = pm.newQuery(VideoSubmission.class);
		query.declareImports("import java.util.Date");
		query.setOrdering("created desc");
		List<VideoSubmission> videoEntries = (List<VideoSubmission>) query
				.execute();

		try {
			String json = Util.GSON.toJson(videoEntries);

			resp.setContentType("text/javascript");
			resp.getWriter().println(json);
		} catch (Exception e) {
			log.warning(e.toString());
			resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
		} finally {
			pm.close();
		}
	}
}
