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

package com.google.ytd.dao;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.ytd.model.Assignment;
import com.google.ytd.util.Util;
import com.google.ytd.youtube.YouTubeApiHelper;

/**
 * Class that handles persisting new assignments to the datastore and creating
 * their associated YouTube playlists.
 */
@Singleton
public class AssignmentDaoImpl implements AssignmentDao {
  private static final Logger log = Logger.getLogger(AssignmentDaoImpl.class.getName());

  @Inject
  private Util util;
  @Inject
  private PersistenceManagerFactory pmf;
  @Inject
  private YouTubeApiHelper youTubeApiHelper;
  @Inject
  private AdminConfigDao adminConfigDao;

  @Inject
  public AssignmentDaoImpl(PersistenceManagerFactory pmf) {
    this.pmf = pmf;
  }

  public Assignment getAssignmentById(long id) {
    PersistenceManager pm = pmf.getPersistenceManager();

    try {
      Assignment assignment = pm.getObjectById(Assignment.class, id);
      return pm.detachCopy(assignment);
    } catch (JDOObjectNotFoundException e) {
      log.log(Level.WARNING, e.getMessage(), e);
      return null;
    } finally {
      pm.close();
    }
  }

  /**
   * Retrieves an Assignment from the datastore given its id.
   * 
   * @param id
   *          An ID corresponding to an Assignment object in the datastore.
   * @return The Assignment object whose id is specified, or null if the id is
   *         invalid.
   */
  public Assignment getAssignmentById(String id) {
    try {
      return getAssignmentById(Long.parseLong(id));
    } catch (NumberFormatException e) {
      log.log(Level.WARNING, "", e);
      return null;
    }
  }

  @Override
  public List<Assignment> getAssignments(String sortBy, String sortOrder, String filterType) {
    PersistenceManager pm = pmf.getPersistenceManager();
    List<Assignment> assignments = null;

    try {
      Query query = pm.newQuery(Assignment.class);
      query.declareImports("import java.util.Date");
      query.declareParameters("String filterType");
      query.setOrdering(sortBy + " " + sortOrder);

      if (!filterType.toUpperCase().equals("ALL")) {
        String filters = "status == filterType";
        query.setFilter(filters);
      }

      assignments = (List<Assignment>) query.execute(filterType);
      assignments = (List<Assignment>) pm.detachCopyAll(assignments);
    } finally {
      pm.close();
    }

    return assignments;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.google.ytd.dao.AssignmentDao#newAssignment(com.google.ytd.model.Assignment
   * )
   */
  public Assignment newAssignment(Assignment assignment) {
    PersistenceManager pm = pmf.getPersistenceManager();
    try {
      assignment = pm.makePersistent(assignment);
      String token = adminConfigDao.getAdminConfig().getYouTubeAuthSubToken();
      if (util.isNullOrEmpty(token)) {
        log.warning(String.format("Could not create new playlist for assignment '%s' because no"
            + " YouTube AuthSub token was found in the config.", assignment.getDescription()));
      } else {
        youTubeApiHelper.setToken(token);
        String playlistTitle = String.format("Playlist #%d %s", assignment.getId(), assignment
            .getDescription());

        String playlistId = youTubeApiHelper.createPlaylist(playlistTitle, assignment
            .getDescription());
        assignment.setPlaylistId(playlistId);
        assignment = pm.makePersistent(assignment);
        assignment = pm.detachCopy(assignment);
      }
    } finally {
      pm.close();
    }
    return assignment;
  }

  public Assignment save(Assignment assignment) {
    PersistenceManager pm = pmf.getPersistenceManager();
    try {
      assignment = pm.makePersistent(assignment);
      assignment = pm.detachCopy(assignment);
    } finally {
      pm.close();
    }
    return assignment;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.google.ytd.dao.AssignmentDao#getDefaultMobileAssignmentId()
   */
  @SuppressWarnings("unchecked")
  public long getDefaultMobileAssignmentId() {
    long assignmentId = -1;
    String defaultMobileAssignmentDescription = "default mobile assignment";
    PersistenceManager pm = pmf.getPersistenceManager();
    try {
      Query query = pm.newQuery(Assignment.class);
      query.declareParameters("String defaultMobileAssignmentDescription");
      query.setFilter("description == defaultMobileAssignmentDescription");
      List<Assignment> results = (List<Assignment>) query
          .execute(defaultMobileAssignmentDescription);
      if (results.size() > 0) {
        assignmentId = results.get(0).getId();
      } else {
        // create the singleton default mobile assignment
        Assignment assignment = new Assignment();
        assignment.setCategory("News");
        assignment.setDescription(defaultMobileAssignmentDescription);
        assignment.setStatus(Assignment.AssignmentStatus.ACTIVE);
        assignment = pm.makePersistent(assignment);

        String token = adminConfigDao.getAdminConfig().getYouTubeAuthSubToken();
        if (util.isNullOrEmpty(token)) {
          log.warning(String.format("Could not create new playlist for assignment '%s' because no"
              + " YouTube AuthSub token was found in the config.", assignment.getDescription()));
        } else {
          youTubeApiHelper.setToken(token);
          String playlistId = youTubeApiHelper.createPlaylist(String.format(
              "Playlist for Assignment #%d", assignment.getId()), assignment.getDescription());
          assignment.setPlaylistId(playlistId);
          assignment = pm.makePersistent(assignment);
        }
        assignmentId = assignment.getId();
      }
    } finally {
      pm.close();
    }
    return assignmentId;
  }
}
