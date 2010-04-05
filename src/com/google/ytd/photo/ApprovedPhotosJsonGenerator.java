package com.google.ytd.photo;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.ytd.dao.PhotoSubmissionDao;
import com.google.ytd.model.PhotoEntry;
import com.google.ytd.model.PhotoSubmission;
import com.google.ytd.model.PhotoEntry.ModerationStatus;
import com.google.ytd.util.Util;

@Singleton
public class ApprovedPhotosJsonGenerator extends HttpServlet {
  private static final Logger LOG = Logger.getLogger(ApprovedPhotosJsonGenerator.class.getName());

  private PhotoSubmissionDao photoSubmissionDao = null;
  private Util util = null;

  @Inject
  public ApprovedPhotosJsonGenerator(PhotoSubmissionDao photoSubmissionDao, Util util) {
    this.photoSubmissionDao = photoSubmissionDao;
    this.util = util;
  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    int count = 20;
    if (!util.isNullOrEmpty(req.getParameter("count"))) {
      count = Integer.parseInt(req.getParameter("count"));
    }

    String submissionId = req.getParameter("id");
    if (util.isNullOrEmpty(submissionId)) {
      throw new IllegalArgumentException("Missing required param: id");
    }

    PhotoSubmission submission = this.photoSubmissionDao.getSubmissionById(submissionId);
    List<PhotoEntry> entries = this.photoSubmissionDao.getAllPhotos(submissionId,
            ModerationStatus.APPROVED);

    String serverHost = req.getServerName();
    int serverPort = req.getServerPort();

    JSONObject json = new JSONObject();

    try {
      json.put("title", submission.getTitle());
      json.put("description", submission.getDescription());
      json.put("author", submission.getAuthor());
      JSONArray jsonArray = new JSONArray();
      json.put("total", entries.size());
      json.put("result", jsonArray);

      for (PhotoEntry entry : entries) {
        JSONObject jsonObject = new JSONObject();
        String imageUrl = "http://" + serverHost + ":" + serverPort + entry.getImageUrl();
        jsonObject.put("imageUrl", imageUrl);
        jsonArray.put(jsonObject);
      }

      resp.setHeader("content-type", "application/javascript");
      resp.getWriter().println(json.toString());
    } catch (JSONException e) {
      LOG.log(Level.SEVERE, "json generation error", e);
    }
  }

}