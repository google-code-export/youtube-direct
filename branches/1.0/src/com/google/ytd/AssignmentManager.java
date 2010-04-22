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

import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.google.inject.Singleton;
import com.google.ytd.model.Assignment;

/**
 * Class that handles persisting new assignments to the datastore and creating their associated
 * YouTube playlists.
 */
@Singleton
public class AssignmentManager {
  private static final Logger log = Logger.getLogger(AssignmentManager.class.getName());
  
  public AssignmentManager() {
  }
  
  public void newAssignment(Assignment assignment, String ipAddress) {    
    PersistenceManager pm = Util.getPersistenceManagerFactory().getPersistenceManager();
    assignment = pm.makePersistent(assignment);
    YouTubeApiManager apiManager = new YouTubeApiManager();
    apiManager.setRequestIpAddress(ipAddress);
    String token = Util.getAdminConfig().getYouTubeAuthSubToken();
    if (Util.isNullOrEmpty(token)) {
      log.warning(String.format("Could not create new playlist for assignment '%s' because no" +
          " YouTube AuthSub token was found in the config.", assignment.getDescription()));
    } else {
      apiManager.setToken(token);
      String playlistId = apiManager.createPlaylist(String.format("Playlist for Assignment #%d",
              assignment.getId()), assignment.getDescription());
      assignment.setPlaylistId(playlistId);
      assignment = pm.makePersistent(assignment);          
    }               
  }
  
  @SuppressWarnings("unchecked")
  public long getDefaultMobileAssignmentId(String ipAddress) {
    PersistenceManager pm = Util.getPersistenceManagerFactory().getPersistenceManager();
    long assignmentId = -1;   
    String defaultMobileAssignmentDescription = "default mobile assignment";     
    try {
      Query query = pm.newQuery(Assignment.class);
      query.declareParameters("String defaultMobileAssignmentDescription");
      query.setFilter("description == defaultMobileAssignmentDescription");
      List<Assignment> results = (List<Assignment>) 
          query.execute(defaultMobileAssignmentDescription);
      if (results.size() > 0) {
        assignmentId = results.get(0).getId();
      } else {
        // create the singleton default mobile assignment
        Assignment assignment = new Assignment();
        assignment.setCategory("News");
        assignment.setDescription(defaultMobileAssignmentDescription);
        assignment.setStatus(Assignment.AssignmentStatus.ACTIVE); 
        newAssignment(assignment, ipAddress);     
        assignmentId = assignment.getId();
      }
    } finally {
      pm.close();
    }
    return assignmentId;
  }  
}
