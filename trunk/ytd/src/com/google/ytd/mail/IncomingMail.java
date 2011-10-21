// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.ytd.mail;

import com.google.appengine.api.NamespaceManager;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions.Method;

import static com.google.appengine.api.taskqueue.TaskOptions.Builder.*;

import com.google.gdata.data.DateTime;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.ytd.dao.AdminConfigDao;
import com.google.ytd.dao.AssignmentDao;
import com.google.ytd.model.DataChunk;
import com.google.ytd.model.PhotoEntry;
import com.google.ytd.model.PhotoSubmission;
import com.google.ytd.util.EmailUtil;
import com.google.ytd.util.PmfUtil;
import com.google.ytd.util.Util;

import org.apache.geronimo.mail.util.Base64DecoderStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Singleton
public class IncomingMail extends HttpServlet {
  private static final Logger LOG = Logger.getLogger(IncomingMail.class.getName());
  
  private static final long TASK_DELAY = 1000 * 30; // Timeout before task is invoked.
  
  @Inject
  private Util util;
  @Inject
  private PmfUtil pmfUtil;
  @Inject
  private EmailUtil emailUtil;
  @Inject
  private AssignmentDao assignmentDao;
  @Inject
  private AdminConfigDao adminConfigDao;

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    LOG.info("Starting up...");

    try {
      if (!adminConfigDao.allowPhotoSubmission()) {
        throw new IllegalStateException("Photo submissions are not enabled.");
      }
      
      Properties props = new Properties();
      Session session = Session.getDefaultInstance(props, null);
      MimeMessage message = new MimeMessage(session, request.getInputStream());
      
      String title = message.getSubject();
      LOG.info(String.format("Subject line is '%s'.", title));
      
      String author = "Unknown";
      String email = "";
      InternetAddress[] fromAddresses = (InternetAddress[]) message.getFrom();
      if (fromAddresses != null && fromAddresses.length > 0) {
        InternetAddress fromAddress = fromAddresses[0];
        LOG.info(String.format("From name is '%s' and address is '%s'.", fromAddress.getPersonal(),
            fromAddress.getAddress()));
        
        if (!util.isNullOrEmpty(fromAddress.getPersonal())) {
          author = fromAddress.getPersonal();
        }
        
        if (util.isNullOrEmpty(fromAddress.getAddress())) {
          throw new IllegalArgumentException("No 'From' email address found.");
        } else {
          email = fromAddress.getAddress();
        }
      }

      String assignmentId;
      String namespace = "";
      Pattern regex = Pattern.compile("/(\\d+)(?:\\+(\\w+))?@");
      Matcher matcher = regex.matcher(request.getRequestURI());
      if (matcher.find()) {
        assignmentId = matcher.group(1);
        if (matcher.groupCount() > 2) {
          namespace = matcher.group(2);
          NamespaceManager.set(namespace);
        }
      } else {
        assignmentId = String.valueOf(assignmentDao.getDefaultMobileAssignmentId());
      }
      
      LOG.info(String.format("Assignment id is '%s' and namespace is '%s'.",
        assignmentId, namespace));
      
      if (!assignmentDao.isAssignmentPhotoEnabled(assignmentId)) {
        throw new IllegalArgumentException(String.format("Assignment id '%s' either does not "
            + "exist, or is not enabled for photo submissions.", assignmentId));
      }
      
      String mimeType = message.getContentType().toLowerCase();
      LOG.info("Incoming message's MIME type is " + mimeType);

      ArrayList<BodyPart> parts = null;
      if (mimeType.startsWith("multipart/")) {
        parts = emailUtil.getAllMIMEParts((MimeMultipart) message.getContent());
      } else if (mimeType.startsWith("image/")) {
        parts = new ArrayList<BodyPart>();
        parts.add((BodyPart) message.getContent());
      }
      
      String description = "Submitted via email.";
      int imageCount = 0;
      
      if (parts != null) {
        for (BodyPart part : parts) {
          String partMimeType = part.getContentType().toLowerCase();
          
          if (partMimeType.startsWith("image/")) {
            imageCount++;
          } else if (partMimeType.startsWith("text/plain")) {
            description = (String) part.getContent();
            LOG.info(String.format("Description is '%s'.", description));
          }
        }
      }
      
      if (imageCount == 0) {
        throw new IllegalArgumentException("No image attachments were found in the email.");
      }
      
      String date = new DateTime().toUiString();
      
      PhotoSubmission photoSubmission = new PhotoSubmission(Long.parseLong(assignmentId), "",
          author, email, "", title, description, "", date, imageCount);
      pmfUtil.persistJdo(photoSubmission);
      String submissionId = photoSubmission.getId();

      imageCount = 0;
      for (BodyPart part : parts) {
        String partMimeType = part.getContentType().toLowerCase();

        if (partMimeType.startsWith("image/") && part.getContent() instanceof Base64DecoderStream) {
          imageCount++;
          // The filename might be appended following a ';' character.
          String[] splits = partMimeType.split(";\\s*");
          partMimeType = splits[0];

          String fileName = "unknown";
          if (splits.length > 1) {
            fileName = splits[1];
          }

          PhotoEntry photoEntry = new PhotoEntry(submissionId,
              String.format("%s%d", submissionId, imageCount), partMimeType);
          pmfUtil.persistJdo(photoEntry);
          
          Base64DecoderStream base64Stream = (Base64DecoderStream) part.getContent();
          
          int index = 0;
          int bytesRead = DataChunk.CHUNK_SIZE;
          long fileSize = 0;
          while (bytesRead == DataChunk.CHUNK_SIZE) {
            byte[] buffer = new byte[DataChunk.CHUNK_SIZE];
            bytesRead = base64Stream.read(buffer, 0, DataChunk.CHUNK_SIZE);
            fileSize += bytesRead;

            if (bytesRead < DataChunk.CHUNK_SIZE) {
              buffer = Arrays.copyOf(buffer, bytesRead);
            }
            
            DataChunk chunk = new DataChunk(photoEntry.getId(), index, buffer);
            pmfUtil.persistJdo(chunk);
            index++;
          }
          
          photoEntry.setOriginalFileSize(fileSize);
          photoEntry.setOriginalFileName(fileName);
          pmfUtil.persistJdo(photoEntry);
        }
      }
      
      Queue queue = QueueFactory.getDefaultQueue();
      queue.add(withUrl("/tasks/MoveToPicasa").method(Method.POST).param("id", submissionId)
        .param("ns", namespace).countdownMillis(TASK_DELAY));
    } catch (MessagingException e) {
      LOG.log(Level.WARNING, "", e);
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
    } catch (IllegalArgumentException e) {
      LOG.log(Level.WARNING, "", e);
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
    } catch (IllegalStateException e) {
      LOG.log(Level.WARNING, "", e);
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }
}
