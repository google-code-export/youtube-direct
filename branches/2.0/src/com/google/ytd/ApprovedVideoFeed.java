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

package com.google.ytd;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.ytd.model.VideoSubmission;
import com.google.ytd.util.Util;

/**
 * Returns JSON data representing approved videos.
 * 
 * Usage of this servlet is not recommended on high-volume sites, as there is a
 * cost involved with every App Engine datastore lookup. Instead, the YouTube
 * API can be used to retrieve similar info.
 */
@Singleton
public class ApprovedVideoFeed extends HttpServlet {
	private static final Logger log = Logger.getLogger(ApprovedVideoFeed.class.getName());

	@Inject
	private Util util;
	@Inject
	private PersistenceManagerFactory pmf;

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		String sortBy = "created";
		String sortOrder = "desc";
		int pageIndex = 1;
		int pageSize = 10;
		int filterType = 1; // approved
		long assignmentId = -1;

		if (req.getParameter("id") != null) {
			assignmentId = Long.parseLong(req.getParameter("id"));
		}

		if (req.getParameter("sortby") != null) {
			sortBy = req.getParameter("sortby");
		}

		if (req.getParameter("sortorder") != null) {
			sortOrder = req.getParameter("sortorder");
		}

		if (req.getParameter("pageindex") != null) {
			pageIndex = Integer.parseInt(req.getParameter("pageindex"));
		}

		if (req.getParameter("pagesize") != null) {
			pageSize = Integer.parseInt(req.getParameter("pagesize"));
		}

		if (req.getParameter("filtertype") != null) {
			filterType = Integer.parseInt(req.getParameter("filtertype"));
		}

		PersistenceManager pm = pmf.getPersistenceManager();

		try {
			Query query = pm.newQuery(VideoSubmission.class);

			query.declareImports("import java.util.Date");
			query.setOrdering(sortBy + " " + sortOrder);
			query.declareParameters("long assignmentId_");

			String filters = "assignmentId == assignmentId_";

			if (filterType > -1) {
				filters += "&& status == " + filterType;
			}

			query.setFilter(filters);

			List<VideoSubmission> videoEntries = (List<VideoSubmission>) query.execute(assignmentId);

			int totalSize = videoEntries.size();
			int totalPages = (int) Math.ceil(((double) totalSize / (double) pageSize));
			int startIndex = (pageIndex - 1) * pageSize; // inclusive
			int endIndex = -1; // exclusive

			if (pageIndex < totalPages) {
				endIndex = startIndex + pageSize;
			} else {
				if (pageIndex == totalPages && totalSize % pageSize == 0) {
					endIndex = startIndex + pageSize;
				} else {
					endIndex = startIndex + (totalSize % pageSize);
				}
			}

			String json = null;
			List returnList = videoEntries.subList(startIndex, endIndex);
			json = util.toJson(returnList);
			json = "{\"total\": \"" + totalSize + "\", \"entries\": " + json + "}";

			resp.setContentType("text/javascript");
			resp.getWriter().println(json);
		} finally {
			pm.close();
		}
	}
}
