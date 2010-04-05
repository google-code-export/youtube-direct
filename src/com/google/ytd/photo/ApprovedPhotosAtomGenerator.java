package com.google.ytd.photo;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.ytd.dao.PhotoSubmissionDao;
import com.google.ytd.model.PhotoEntry;
import com.google.ytd.model.PhotoSubmission;
import com.google.ytd.model.PhotoEntry.ModerationStatus;
import com.google.ytd.util.Util;

@Singleton
public class ApprovedPhotosAtomGenerator extends HttpServlet {
  private static final Logger LOG = Logger.getLogger(ApprovedPhotosAtomGenerator.class.getName());

  private PhotoSubmissionDao photoSubmissionDao = null;
  private Util util = null;

  @Inject
  public ApprovedPhotosAtomGenerator(PhotoSubmissionDao photoSubmissionDao, Util util) {
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

    String author = submission.getAuthor();
    String title = submission.getTitle();
    Date updated = submission.getUpdated();
    String description = submission.getDescription();

    String serverHost = req.getServerName();
    int serverPort = req.getServerPort();

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ssZ");

    StringBuffer xml = new StringBuffer();
    xml.append("<feed xmlns=\"http://www.w3.org/2005/Atom\">");
    xml.append(String.format("<id>http://%s/feeds/atom?id=%s</id>", serverHost, submissionId));
    xml.append(String.format("<title type=\"text\">%s</title>", title));
    xml.append(String.format("<subtitle type=\"text\">%s</subtitle>", description));
    xml.append(String.format("<link href=\"http://%s/feeds/atom?id=%s\" rel=\"self\"></link>",
            serverHost, submissionId));
    xml.append(String.format("<author><name></name></author>", author));
    xml.append(String.format("<updated>%s</updated>", String.format("%sT%s", dateFormat
            .format(updated), timeFormat.format(updated))));

    for (PhotoEntry entry : entries) {
      String id = URLEncoder.encode(entry.getId(), "UTF-8");

      String imageUrl = "http://" + serverHost + ":" + serverPort + entry.getImageUrl();
      String thumbnailUrl = "http://" + serverHost + ":" + serverPort + entry.getThumbnailUrl();

      xml.append("<entry>");
      xml.append(String.format("<id>http://%s/post/%s</id>", serverHost, id));
      xml.append(String.format("<title type=\"text\">%s</title>", title));
      xml.append(String.format("<link href=\"http://%s/post/%s\" rel=\"self\"></link>", serverHost,
              id));
      xml.append(String.format("<author><name>%s</name></author>", author));
      xml.append(String.format("<updated>%s</updated>", String.format("%sT%s", dateFormat
              .format(updated), timeFormat.format(updated))));

      StringBuffer content = new StringBuffer();
      content.append(String.format("<a href=\"%s\" xmlns=\"http://www.w3.org/1999/xhtml\">%s</a>",
              imageUrl, imageUrl));
      content
              .append("<br xmlns=\"http://www.w3.org/1999/xhtml\"/><br xmlns=\"http://www.w3.org/1999/xhtml\"/>");
      content
              .append(String
                      .format(
                              "<a xmlns=\"http://www.w3.org/1999/xhtml\" target=\"_blank\" href=\"%s\"><img xmlns=\"http://www.w3.org/1999/xhtml\" src=\"%s\"/></a>",
                              imageUrl, thumbnailUrl));

      xml.append(String.format("<content type=\"application/xhtml+xml\">%s</content>", content
              .toString()));
      xml.append("</entry>");
    }

    xml.append("</feed>");

    resp.setHeader("content-type", "application/atom+xml");
    resp.getWriter().println(xml.toString());
  }

}