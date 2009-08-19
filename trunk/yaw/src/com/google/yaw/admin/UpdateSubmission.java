package com.google.yaw.admin;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.yaw.Util;
import com.google.yaw.model.Assignment;
import com.google.yaw.model.VideoSubmission;

public class UpdateSubmission extends HttpServlet {

    private static final Logger log = Logger.getLogger(UpdateSubmission.class
                    .getName());    
    
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
    throws IOException {
        
    	PersistenceManagerFactory pmf = Util.getPersistenceManagerFactory();
        PersistenceManager pm = pmf.getPersistenceManager();
        
    	try {
	    	String json = Util.getPostBody(req);
	    		    		    	
	    	VideoSubmission entry = null;
	    	
	    	VideoSubmission jsonObj = Util.GSON.fromJson(json, VideoSubmission.class);	    		    	
	    	
	    	String id = jsonObj.getId();
	    	
	    	entry = (VideoSubmission) pm.getObjectById(VideoSubmission.class, id);
			
			entry.setVideoId(jsonObj.getVideoId());
			entry.setStatus(jsonObj.getStatus());
			entry.setVideoTitle(jsonObj.getVideoTitle());
			entry.setVideoDescription(jsonObj.getVideoDescription());
			entry.setVideoTags(jsonObj.getVideoTags());
			entry.setUpdated(new Date());
			
			pm.makePersistent(entry);					
			
            resp.setContentType("text/javascript");
            resp.getWriter().println(Util.GSON.toJson(entry));
            
        } catch(Exception e) {
            log.warning(e.toString());
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } finally {
            pm.close();
        }
    }
}
