package com.google.ytd.image;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.ytd.dao.PhotoSubmissionDao;
import com.google.ytd.model.PhotoEntry;

@Singleton
public class ServeThumbnail extends HttpServlet {
  private static final Logger log = Logger.getLogger(ServeThumbnail.class.getName());

  private PhotoSubmissionDao photoSubmissionDao = null;
  
  @Inject
  public ServeThumbnail(PhotoSubmissionDao photoSubmissionDao) {
    this.photoSubmissionDao = photoSubmissionDao;
  }
  
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String id = req.getParameter("id");    
    PhotoEntry entry = photoSubmissionDao.getPhotoEntry(id);    
    resp.setContentType("image/" + entry.getFormat());
    resp.getOutputStream().write(entry.getThumbnail());    
  }
}