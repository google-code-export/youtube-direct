package com.google.yaw;

import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.yaw.model.Assignment;

@Singleton
public class AssignmentManager {
  
  private static final Logger log = Logger.getLogger(AssignmentManager.class.getName());
  
  public AssignmentManager() {
  }
  
  public void newAssignment(Assignment assignment) {    
    PersistenceManager pm = Util.getPersistenceManagerFactory().getPersistenceManager();
    assignment = pm.makePersistent(assignment);
    YouTubeApiManager apiManager = new YouTubeApiManager();
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
  public long getDefaultMobileAssignmentId() {
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
        newAssignment(assignment);     
        assignmentId = assignment.getId();
      }
    } finally {
      pm.close();
    }
    return assignmentId;
  }  
}
