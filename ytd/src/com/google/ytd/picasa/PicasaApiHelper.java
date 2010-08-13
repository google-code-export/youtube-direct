/*
 * Copyright (c) 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.ytd.picasa;

import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.utils.SystemProperty;
import com.google.gdata.client.photos.PicasawebService;
import com.google.gdata.data.ParseSource;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.photos.AlbumEntry;
import com.google.gdata.data.photos.AlbumFeed;
import com.google.gdata.data.photos.GphotoAccess;
import com.google.gdata.data.photos.PhotoEntry;
import com.google.gdata.data.photos.UserFeed;
import com.google.gdata.util.ParseUtil;
import com.google.gdata.util.ServiceException;
import com.google.inject.Inject;
import com.google.ytd.dao.AdminConfigDao;
import com.google.ytd.dao.AssignmentDao;
import com.google.ytd.util.Util;

import org.apache.commons.lang.StringEscapeUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class to handle interfacing with the Google Data Java Client Library's Picasa
 * support.
 */
public class PicasaApiHelper {
  private static final Logger LOG = Logger.getLogger(PicasaApiHelper.class.getName());

  // CONSTANTS
  private static final String USER_FEED_URL =
      "http://picasaweb.google.com/data/feed/api/user/default";
  private static final String RESUMABLE_UPLOADS_URL_FORMAT =
    "http://picasaweb.google.com/data/upload/resumable/photos/create-session/feed/api/user/default/albumid/%s";
  private static final String UPLOAD_ENTRY_XML_FORMAT = 
    "<?xml version='1.0' encoding='UTF-8'?>\n"
    + "<entry xmlns='http://www.w3.org/2005/Atom'>"
    + "\n  <title>%s</title>"
    + "\n  <summary>%s</summary>"
    + "\n  <category scheme='http://schemas.google.com/g/2005#kind' "
    + "term='http://schemas.google.com/photos/2007#photo'/>"
    + "\n</entry>";
  // The connect + read timeout needs to be <= 10 seconds, due to App Engine limitations.
  private static final int CONNECT_TIMEOUT = 1000 * 2; // In milliseconds
  private static final int READ_TIMEOUT = 1000 * 8; // In milliseconds
  // The size of each resumable upload chunk we send. Due to App Engine limitations, this needs to
  // be less than 1MB.
  private static final int CHUNK_SIZE = 950 * 1024; // 950KB

  private PicasawebService service = null;
  private Util util = null;
  private AdminConfigDao adminConfigDao = null;

  @Inject
  public PicasaApiHelper(AdminConfigDao adminConfigDao, AssignmentDao assignmentDao) {
    this.service = new PicasawebService(Util.CLIENT_ID_PREFIX + SystemProperty.applicationId.get());
    this.util = Util.get();
    this.adminConfigDao = adminConfigDao;

    String authSubToken = adminConfigDao.getAdminConfig().getPicasaAuthSubToken();
    if (!util.isNullOrEmpty(authSubToken)) {
      service.setAuthSubToken(authSubToken);
    }

    service.setConnectTimeout(CONNECT_TIMEOUT);
    service.setReadTimeout(READ_TIMEOUT);
  }

  public boolean isAuthenticated() {
    return service.getAuthTokenFactory().getAuthToken() != null;
  }

  public void setAuthSubToken(String token) {
    service.setAuthSubToken(token);
  }

  public String getCurrentUsername() throws IOException, ServiceException {
    try {
      UserFeed userFeed = service.getFeed(new URL(USER_FEED_URL), UserFeed.class);
      return userFeed.getUsername();
    } catch (MalformedURLException e) {
      LOG.log(Level.WARNING, "", e);
    }

    return null;
  }

  public String createAlbum(String title, String description, boolean privateAlbum) {
    LOG.info(String.format("Attempting to create %s Picasa album...",
        privateAlbum ? "private" : "public"));
    AlbumEntry album = new AlbumEntry();

    if (privateAlbum) {
      album.setAccess(GphotoAccess.Value.PRIVATE);
    } else {
      album.setAccess(GphotoAccess.Value.PUBLIC);
    }

    album.setTitle(new PlainTextConstruct(title));
    album.setDescription(new PlainTextConstruct(description));

    try {
      AlbumEntry albumEntry = service.insert(new URL(USER_FEED_URL), album);
      String albumUrl = albumEntry.getFeedLink().getHref();
      LOG.info(String.format("Created %s Picasa album: %s",
          privateAlbum ? "private" : "public", albumUrl));

      return albumUrl;
    } catch (MalformedURLException e) {
      LOG.log(Level.WARNING, "", e);
    } catch (IOException e) {
      LOG.log(Level.WARNING, "", e);
    } catch (ServiceException e) {
      LOG.log(Level.WARNING, "", e);
    }

    return null;
  }

  public String moveToNewAlbum(String photoUrl, String newAlbumUrl) {
    LOG.info(String.format("Preparing to move '%s' to album '%s'...", photoUrl, newAlbumUrl));

    AlbumFeed albumFeed = getAlbumFeedFromUrl(newAlbumUrl);
    if (albumFeed == null) {
      throw new IllegalArgumentException(String.format("Could not retrieve album from URL '%s'.",
          newAlbumUrl));
    }
    String newAlbumId = albumFeed.getGphotoId();

    com.google.gdata.data.photos.PhotoEntry photoEntry = getPhotoEntryFromUrl(photoUrl);
    if (photoEntry == null) {
      throw new IllegalArgumentException(String.format("Could not get photo from URL '%s'.",
          photoUrl));
    }

    photoEntry.setAlbumId(newAlbumId);
    try {
      photoEntry = photoEntry.update();
      LOG.info("Move was successful.");

      return photoEntry.getEditLink().getHref();
    } catch (IOException e) {
      LOG.log(Level.WARNING, "", e);
    } catch (ServiceException e) {
      LOG.log(Level.WARNING, "", e);
    }

    return null;
  }

  private com.google.gdata.data.photos.PhotoEntry getPhotoEntryFromUrl(String photoUrl) {
    try {
      return service.getEntry(new URL(photoUrl), com.google.gdata.data.photos.PhotoEntry.class);
    } catch (MalformedURLException e) {
      LOG.log(Level.WARNING, "", e);
    } catch (IOException e) {
      LOG.log(Level.WARNING, "", e);
    } catch (ServiceException e) {
      LOG.log(Level.WARNING, "", e);
    }

    return null;
  }

  private AlbumFeed getAlbumFeedFromUrl(String albumUrl) {
    try {
      return service.getFeed(new URL(albumUrl), AlbumFeed.class);
    } catch (MalformedURLException e) {
      LOG.log(Level.WARNING, "", e);
    } catch (IOException e) {
      LOG.log(Level.WARNING, "", e);
    } catch (ServiceException e) {
      LOG.log(Level.WARNING, "", e);
    }

    return null;
  }

  public String getResumableUploadUrl(com.google.ytd.model.PhotoEntry photoEntry, String title,
      String description, String albumId) throws IllegalArgumentException {
    LOG.info(String.format("Resumable upload request.\nTitle: %s\nDescription: %s\nAlbum: %s",
        title, description, albumId));
    
    // Picasa API resumable uploads are not currently documented publicly, but they're essentially
    // the same as what YouTube API offers:
    // http://code.google.com/apis/youtube/2.0/developers_guide_protocol_resumable_uploads.html
    // The Java client library does offer support for resumable uploads, but its use of threads
    // and some other assumptions makes it unsuitable for our purposes.
    try {
      URL url = new URL(String.format(RESUMABLE_UPLOADS_URL_FORMAT, albumId));
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setDoOutput(true);
      connection.setConnectTimeout(CONNECT_TIMEOUT);
      connection.setReadTimeout(READ_TIMEOUT);
      connection.setRequestMethod("POST");

      // Set all the GData request headers. These strings should probably be moved to CONSTANTS.
      connection.setRequestProperty("Content-Type", "application/atom+xml;charset=UTF-8");
      connection.setRequestProperty("Authorization", String.format("AuthSub token=\"%s\"", adminConfigDao.getAdminConfig().getPicasaAuthSubToken()));
      connection.setRequestProperty("GData-Version", "2.0");
      connection.setRequestProperty("Slug", photoEntry.getOriginalFileName());
      connection.setRequestProperty("X-Upload-Content-Type", photoEntry.getFormat());
      connection.setRequestProperty("X-Upload-Content-Length",
          String.valueOf(photoEntry.getOriginalFileSize()));
      
      String atomXml = String.format(UPLOAD_ENTRY_XML_FORMAT, StringEscapeUtils.escapeXml(title),
          StringEscapeUtils.escapeXml(description));

      OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
      writer.write(atomXml);
      writer.close();
      
      if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
        String uploadUrl = connection.getHeaderField("Location");
        if (util.isNullOrEmpty(uploadUrl)) {
          throw new IllegalArgumentException("No Location header found in HTTP response.");
        } else {
          LOG.info("Resumable upload URL is " + uploadUrl);
        
          return uploadUrl;
        }
      } else {
        LOG.warning(String.format("HTTP POST to %s returned status %d (%s).", url.toString(),
            connection.getResponseCode(), connection.getResponseMessage()));
      }
    } catch (MalformedURLException e) {
      LOG.log(Level.WARNING, "", e);
      
      throw new IllegalArgumentException(e);
    } catch (IOException e) {
      LOG.log(Level.WARNING, "", e);
    }

    return null;
  }
  
  public PhotoEntry doResumableUpload(com.google.ytd.model.PhotoEntry photoEntry)
      throws IllegalArgumentException {
    if (util.isNullOrEmpty(photoEntry.getResumableUploadUrl())) {
      throw new IllegalArgumentException(String.format("No resumable upload URL found for "
          + "PhotoEntry id '%s'.", photoEntry.getId()));
    }

    try {
      URL url = new URL(photoEntry.getResumableUploadUrl());

      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setInstanceFollowRedirects(false);
      connection.setConnectTimeout(CONNECT_TIMEOUT);
      connection.setReadTimeout(READ_TIMEOUT);
      connection.setRequestMethod("PUT");

      connection.setRequestProperty("Content-Range", "bytes */*");

      // Response code 308 is specific to this use case and doesn't appear to have a
      // HttpURLConnection constant.
      if (connection.getResponseCode() == 308) {
        long previousByte = 0;

        String rangeHeader = connection.getHeaderField("Range");
        if (!util.isNullOrEmpty(rangeHeader)) {
          LOG.info("Range header in 308 response is " + rangeHeader);
          
          String[] rangeHeaderSplits = rangeHeader.split("-", 2);
          if (rangeHeaderSplits.length == 2) {
            previousByte = Long.valueOf(rangeHeaderSplits[1]).longValue() + 1;
          }
        }

        long lastByte = previousByte + CHUNK_SIZE;
        if (lastByte > (photoEntry.getOriginalFileSize() - 1)) {
          lastByte = photoEntry.getOriginalFileSize() - 1;
        }

        connection = (HttpURLConnection) url.openConnection();
        connection.setInstanceFollowRedirects(false);
        connection.setDoOutput(true);
        connection.setConnectTimeout(CONNECT_TIMEOUT);
        connection.setReadTimeout(READ_TIMEOUT);
        connection.setRequestMethod("PUT");

        String contentRangeHeader = String.format("bytes %d-%d/%d", previousByte, lastByte,
            photoEntry.getOriginalFileSize());
        LOG.info("Using the following for Content-Range header: " + contentRangeHeader);
        connection.setRequestProperty("Content-Range", contentRangeHeader);

        BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();

        OutputStream outputStream = connection.getOutputStream();
        outputStream.write(
            blobstoreService.fetchData(photoEntry.getBlobKey(), previousByte, lastByte));
        outputStream.close();

        if (connection.getResponseCode() == HttpURLConnection.HTTP_CREATED) {
          LOG.info("Resumable upload is complete and successful.");

          return (PhotoEntry) ParseUtil.readEntry(new ParseSource(connection.getInputStream()));
        }
      } else if (connection.getResponseCode() == HttpURLConnection.HTTP_CREATED) {
        // It's possible that the Picasa upload associated with the specific resumable upload URL
        // had previously completed successfully. In that case, the response to the initial */* PUT
        // will be a 201 Created with the new PhotoEntry. This is probably an edge case.
        LOG.info("Resumable upload is complete and successful.");

        return (PhotoEntry) ParseUtil.readEntry(new ParseSource(connection.getInputStream()));
      } else {
        // The IllegalArgumentException should be treated by the calling code as
        // something that is not recoverable, which is to say the resumable upload attempt
        // should be stopped.
        throw new IllegalArgumentException(String.format("HTTP POST to %s returned status %d (%s).",
            url.toString(), connection.getResponseCode(), connection.getResponseMessage()));
      }
    } catch (MalformedURLException e) {
      LOG.log(Level.WARNING, "", e);
      
      throw new IllegalArgumentException(e);
    } catch (IOException e) {
      LOG.log(Level.WARNING, "", e);
    } catch (ServiceException e) {
      LOG.log(Level.WARNING, "", e);
    }

    return null;
  }
}