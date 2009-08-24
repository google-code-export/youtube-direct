package com.google.yaw.admin;

import com.google.yaw.Util;
import com.google.yaw.model.Assignment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet to handle requests that fetch Assignments from the datastore.
 * 
 * <p>Handles both requests to fetch all assignment, and requests for a subset of assignments
 * (i.e. filtered with search parameters or paged requests).
 */
public class GetAllAssignments extends HttpServlet {
  private static final Logger log = Logger.getLogger(GetAllAssignments.class.getName());

  @SuppressWarnings("unchecked")
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    PersistenceManagerFactory pmf = Util.getPersistenceManagerFactory();
    PersistenceManager pm = pmf.getPersistenceManager();

    try {
      String search = req.getParameter("_search");
      String sortColumn = req.getParameter("sidx");
      String sortOrder = req.getParameter("sord");
      String pageNumberParam = req.getParameter("page");
      String pageSizeParam = req.getParameter("rows");

      if (Util.isNullOrEmpty(sortColumn)) {
        throw new IllegalArgumentException("'sidx' parameter is null or empty.");
      }
      if (Util.isNullOrEmpty(sortOrder)) {
        throw new IllegalArgumentException("'sord' parameter is null or empty.");
      }
      if (Util.isNullOrEmpty(pageNumberParam)) {
        throw new IllegalArgumentException("'page' parameter is null or empty.");
      }
      if (Util.isNullOrEmpty(pageSizeParam)) {
        throw new IllegalArgumentException("'rows' parameter is null or empty.");
      }
      
      int pageNumber = Integer.parseInt(pageNumberParam);
      int pageSize = Integer.parseInt(pageSizeParam);

      // Query the datastore for all Assignments, with the desired sort order.
      Query query = pm.newQuery(Assignment.class);
      query.setOrdering(String.format("%s %s", sortColumn, sortOrder));
      List<Assignment> assignments = (List<Assignment>)query.execute();

      if (search != null && search.equals("true")) {
        // The request is for a filtered subset of the full results.
        String searchColumn = req.getParameter("searchField");
        String searchString = req.getParameter("searchString");
        String searchType = req.getParameter("searchOper");

        if (Util.isNullOrEmpty(searchColumn)) {
          throw new IllegalArgumentException("'searchField' parameter is null or empty.");
        }
        if (Util.isNullOrEmpty(searchString)) {
          throw new IllegalArgumentException("'searchString' parameter is null or empty.");
        }
        if (Util.isNullOrEmpty(searchType)) {
          throw new IllegalArgumentException("'searchOper' parameter is null or empty.");
        }

        // Mimic case-insensitive search by converting everything to lower case.
        searchString = searchString.toLowerCase();
        List<Assignment> filteredAssignments = new ArrayList<Assignment>();
        for (Assignment assignment : assignments) {
          String value;
          if (searchColumn.equals("id")) {
            value = assignment.getId().toLowerCase();
          } else if (searchColumn.equals("description")) {
            value = assignment.getDescription().toLowerCase();
          } else if (searchColumn.equals("category")) {
            value = assignment.getCategory().toLowerCase();
          } else if (searchColumn.equals("status")) {
            value = assignment.getStatus().toString().toLowerCase();
          } else {
            throw new IllegalArgumentException(String.format("'%s' is not a valid value for " +
                    "parameter 'searchField'.", searchColumn));
          }

          Boolean matches = false;
          if (searchType.equals("cn") && value.indexOf(searchString) != -1) {
            matches = true;
          } else if (searchType.equals("nc") && value.indexOf(searchString) == -1) {
            matches = true;
          } else if (searchType.equals("eq") && value.equals(searchString)) {
            matches = true;
          } else if (searchType.equals("ne") && !value.equals(searchString)) {
            matches = true;
          }

          if (matches) {
            // We can't modify the assignments list, so copy to a temporary list instead.
            filteredAssignments.add(assignment);
          }
        }

        // Replace the reference to the full list with the temporary list.
        assignments = filteredAssignments;
      }

      int totalResults = assignments.size();
      
      // If request is for 3rd page of results, and each page has 5 results, the first result to
      // return would be index 10.
      int startIndex = (pageNumber - 1) * pageSize;

      // 1 greater than the index of the last element to include, because the second parameter to
      // List.subList() is interpretted as being exclusive. If request is for 3rd page of results,
      // and each page has 5 results, the index value to use would be 15.
      int stopIndex = pageNumber * pageSize;
      
      // Total number of pages is equal to the integer greater than the total divided by page size.
      // If there are 34 total results and a page size of 5, total pages is 7.
      int totalPages = (int)Math.ceil((double)totalResults / (double)pageSize);
      
      if (stopIndex > totalResults) {
        // If this is the last page of results than the calculated index might be bigger than the
        // last valid index in the list.
        stopIndex = totalResults;
      }
      
      // 0-based. startIndex is inclusive, stopIndex is exclusive.
      assignments = assignments.subList(startIndex, stopIndex);

      // The JSON response matches the format expected by jqGrid, as documented at
      // http://www.trirand.com/jqgridwiki/doku.php?id=wiki:retrieving_data#json_data
      JSONObject jsonRepsonse = new JSONObject();
      jsonRepsonse.put("total", totalPages);
      jsonRepsonse.put("page", pageNumber);
      jsonRepsonse.put("records", totalResults);

      JSONArray rows = new JSONArray();
      for (Assignment assignment : assignments) {
        String id = assignment.getId();
        String created = assignment.getCreated().toString();
        String description = assignment.getDescription();
        String category = assignment.getCategory();
        String status = assignment.getStatus().toString();
        String submissionCount = Integer.toString(assignment.getSubmissionCount());

        JSONObject row = new JSONObject();
        // Each row needs a unique id in addition to the user-defined "id" column. Instead of using
        // some incremental number, use datastore id, which should be unique.
        row.put("id", id);

        List<String> data = new ArrayList<String>();
        data.add(id);
        data.add(created);
        data.add(description);
        data.add(category);
        data.add(status);
        data.add(submissionCount);
        row.put("cell", data);

        rows.put(row);
      }
      jsonRepsonse.put("rows", rows);

      resp.setContentType("text/javascript");
      resp.getWriter().println(jsonRepsonse);
    } catch (JSONException e) {
      log.log(Level.WARNING, "", e);
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
    } finally {
      pm.close();
    }
  }
}
