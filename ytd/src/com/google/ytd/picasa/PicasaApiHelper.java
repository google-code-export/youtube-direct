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
import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.Transform;
import com.google.appengine.api.utils.SystemProperty;
import com.google.gdata.client.photos.PicasawebService;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.media.MediaStreamSource;
import com.google.gdata.data.media.mediarss.MediaKeywords;
import com.google.gdata.data.photos.AlbumEntry;
import com.google.gdata.data.photos.AlbumFeed;
import com.google.gdata.data.photos.GphotoAccess;
import com.google.gdata.data.photos.UserFeed;
import com.google.gdata.util.ServiceException;
import com.google.inject.Inject;
import com.google.ytd.dao.AdminConfigDao;
import com.google.ytd.dao.AssignmentDao;
import com.google.ytd.util.Util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
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
	private static final String USER_FEED_URL = "http://picasaweb.google.com/data/feed/api/user/default";
	// The maximum length or width of a resized image.
	private static final int RESIZE_DIMENSION = 600;
	// The largest number of bytes we can safely send to Picasa from App Engine.
	// Let's use 1000KB instead of 1MB to account for metadata overhead.
	private static final int MAX_UPLOAD_BYTES = 1024 * 1000;
	// The connect + read timeout needs to be <= 10 seconds, due to App Engine
	// limitations.
	private static final int CONNECT_TIMEOUT = 1000 * 2; // In milliseconds
	private static final int READ_TIMEOUT = 1000 * 8; // In milliseconds

	private PicasawebService service = null;
	private AdminConfigDao adminConfigDao = null;
	private Util util = null;

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
		LOG.info(String.format("Attempting to create %s Picasa album...", privateAlbum ? "private"
				: "public"));
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
			LOG.info(String.format("Created %s Picasa album: %s", privateAlbum ? "private" : "public",
					albumUrl));

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

	public com.google.gdata.data.photos.PhotoEntry uploadToPicasa(
			com.google.ytd.model.PhotoEntry photoEntry, String title, String description, String albumUrl)
			throws IOException, ServiceException {
		LOG.info(String.format("Preparing to upload to Picasa.\nTitle: %s\nDescription: %s\nAlbum: %s",
				title, description, albumUrl));

		com.google.gdata.data.photos.PhotoEntry picasaPhoto = new com.google.gdata.data.photos.PhotoEntry();

		picasaPhoto.setTitle(new PlainTextConstruct(title));
		picasaPhoto.setDescription(new PlainTextConstruct(description));
		picasaPhoto.setClient(Util.CLIENT_ID_PREFIX + SystemProperty.applicationId.get());

		MediaKeywords keywords = new MediaKeywords();
		keywords.addKeyword(Util.CLIENT_ID_PREFIX + SystemProperty.applicationId.get());

		String defaultTag = adminConfigDao.getAdminConfig().getDefaultTag();
		if (!util.isNullOrEmpty(defaultTag)) {
			keywords.addKeyword(defaultTag);
		}

		picasaPhoto.setKeywords(keywords);

		LOG.info(String.format("Original photo is %d bytes.", photoEntry.getOriginalFileSize()));

		byte[] photoBytes = null;
		if (photoEntry.getOriginalFileSize() > MAX_UPLOAD_BYTES) {
			Image originalImage = ImagesServiceFactory.makeImageFromBlob(photoEntry.getBlobKey());
			ImagesService imagesService = ImagesServiceFactory.getImagesService();
			Transform resize = ImagesServiceFactory.makeResize(RESIZE_DIMENSION, RESIZE_DIMENSION);
			Image resizedImage = imagesService.applyTransform(resize, originalImage);
			photoBytes = resizedImage.getImageData();
			
			LOG.info(String.format("Resized photo is %d bytes.", photoBytes.length));
		} else {
			BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
			photoBytes = blobstoreService.fetchData(photoEntry.getBlobKey(), 0, photoEntry
					.getOriginalFileSize() - 1);
		}

		MediaStreamSource mediaStream = new MediaStreamSource(new ByteArrayInputStream(photoBytes),
				photoEntry.getFormat());
		picasaPhoto.setMediaSource(mediaStream);

		try {
			picasaPhoto = service.insert(new URL(albumUrl), picasaPhoto);
			LOG.info(String
					.format("Upload successful. Url is '%s'.", picasaPhoto.getEditLink().getHref()));

			return picasaPhoto;
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException(e);
		}
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
}
