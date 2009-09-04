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
import javax.jdo.Query;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet to handle requests that fetch Assignments from the datastore.
 * 
 * Handles both requests to fetch all assignment, and requests for a subset of
 *  assignments (i.e. filtered with search parameters or paged requests).
 */
public class GetAllAssignments extends HttpServlet {
  private static final Logger log = Logger.getLogger(GetAllAssignments.class.getName());

  /**
   * Handles searching through a list of Assignments and filtering out the ones that match a
   *  specific criterion.
   * 
   * @param assignments The source list of Assignments to search through.
   * @param searchColumn The name of the column to search on, which is mapped into a property of
   *  Assignment. Currently supported:
   *  <ul>
   *    <li>id</li>
   *    <li>description</li>
   *    <li>category</li>
   *    <li>status</li>
   *  </ul>
   * @param searchString The specific text to search for.
   * @param searchType The type of search to perform. These abbrevations come from the jqGrid
   *  library's built in search. Currently supported:
   *  <ul>
   *    <li>cn (contains)</li>
   *    <li>nc (doesn't contain)</li>
   *    <li>eq (equals)</li>
   *    <li>ne (not equals)</li>
   *  </ul>
   * @return A new list (not a modified version of the assignments parameter) containing the
   *  Assignments that match the search. The list may be empty.
   */
  private List<Assignment> performSearch(List<Assignment> assignments, String searchColumn,
          String searchString, String searchType) {
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
        throw new IllegalArgumentException(String.format("'%s' is not a valid value for "
                + "parameter 'searchField'.", searchColumn));
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

    return filteredAssignments;
  }

  /**
   * Retrieves a list of all Assignments from the datastore and optionally filters them based on
   *  search criteria.
   * 
   * @param sortColumn The name of the Assignment property on which to sort the results.
   * @param sortOrder Either "asc" (ascending) or "desc" (descending).
   * @param searchColumn The name of the column to search on, which is mapped into a property of
   *  Assignment. Currently supported:
   *  <ul>
   *    <li>id</li>
   *    <li>description</li>
   *    <li>category</li>
   *    <li>status</li>
   *  </ul>
   * @param searchString The specific text to search for.
   * @param searchType The type of search to perform. These abbrevations come from the jqGrid
   *  library's built in search. Currently supported:
   *  <ul>
   *    <li>cn (contains)</li>
   *    <li>nc (doesn't contain)</li>
   *    <li>eq (equals)</li>
   *    <li>ne (not equals)</li>
   *  </ul>
   * @return The list of matching Assignments, or all Assignments if no search terms are used.
   * @throws IllegalArgumentException
   */
  @SuppressWarnings("unchecked")
  private List<Assignment> getFilteredAssignments(String sortColumn, String sortOrder,
          String searchColumn, String searchString, String searchType)
          throws IllegalArgumentException {
    PersistenceManager pm = Util.getPersistenceManagerFactory().getPersistenceManager();

    try {
      if (Util.isNullOrEmpty(sortColumn)) {
        throw new IllegalArgumentException("sortColumn parameter is null or empty.");
      }
      if (Util.isNullOrEmpty(sortOrder)) {
        throw new IllegalArgumentException("sortOrder parameter is null or empty.");
      }

      // Query the datastore for all Assignments, with the desired sort order.
      Query query = pm.newQuery(Assignment.class);
      query.setOrdering(String.format("%s %s", sortColumn, sortOrder));
      List<Assignment> assignments = (List<Assignment>) query.execute();
      // Detach all items so that they can be accessed after the PM is closed.
      pm.detachCopyAll(assignments);

      if (!Util.isNullOrEmpty(searchColumn) && !Util.isNullOrEmpty(searchString) &&
              !Util.isNullOrEmpty(searchType)) {
        // Search the results for matching values if we have search parameters.
        assignments = performSearch(assignments, searchColumn, searchString, searchType);
      }

      return assignments;
    } finally {
      pm.close();
    }
  }
  
  /**
   * Takes a list of Assignments and divies it up into a sub-list, representing one page of results.
   * 
   * @param assignments The list of Assignments to be paged.
   * @param pageNumber The number of the page requested, with 1 specifying the first page.
   * @param pageSize The number of Assignments that should appear in each page.
   * @return A list of Assignments representing the page of results. The page will contain at most
   *  pageSize items, with potentially fewer if the page requested is the last one.
   */
  private List<Assignment> pageResults(List<Assignment> assignments, int pageNumber,
          int pageSize) {
    int totalResults = assignments.size();
    
    // If request is for 3rd page of results, and each page has 5 results, the first result to
    // return would be index 10.
    int startIndex = (pageNumber - 1) * pageSize;

    // 1 greater than the index of the last element to include, because the second parameter to
    // List.subList() is interpretted as being exclusive. If request is for 3rd page of results,
    // and each page has 5 results, the index value to use would be 15.
    int stopIndex = pageNumber * pageSize;

    if (stopIndex > totalResults) {
      // If this is the last page of results than the calculated index might
      // be bigger than the
      // last valid index in the list.
      stopIndex = totalResults;
    }

    // 0-based. startIndex is inclusive, stopIndex is exclusive.
    return assignments.subList(startIndex, stopIndex);
  }
  
  /**
   * Takes a list of Assignments and some metadata about the results, and returns a JSON string in a
   *  format that jqGrid expects.
   * 
   * @param assignments The list of Assignments in the results.
   * @param totalPages The total number of pages of results.
   * @param pageNumber The number of this specific page of results.
   * @param totalResults The total number of results.
   * @return JSON data, serialized as a string.
   * @throws JSONException
   */
  private String generateJSONResponse(List<Assignment> assignments, int totalPages, int pageNumber,
          int totalResults) throws JSONException {
    // The JSON response matches the format expected by jqGrid, as documented at
    // http://www.trirand.com/jqgridwiki/doku.php?id=wiki:retrieving_data#json_data
    JSONObject jsonResponse = new JSONObject();
    jsonResponse.put("total", totalPages);
    jsonResponse.put("page", pageNumber);
    jsonResponse.put("records", totalResults);

    JSONArray rows = new JSONArray();
    for (Assignment assignment : assignments) {
      String id = assignment.getId();
      String created = assignment.getCreated().toString();
      String description = assignment.getDescription();
      String category = assignment.getCategory();
      String status = assignment.getStatus().toString();
      String submissionCount = Integer.toString(assignment.getSubmissionCount());

      JSONObject row = new JSONObject();
      // Each row needs a unique id in addition to the user-defined "id"
      // column. Instead of using
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
    jsonResponse.put("rows", rows);
    
    return jsonResponse.toString();
  }
  
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    try {
      String sortColumn = req.getParameter("sidx");
      String sortOrder = req.getParameter("sord");
      String searchColumn = req.getParameter("searchField");
      String searchString = req.getParameter("searchString");
      String searchType = req.getParameter("searchOper");
      
      String pageNumberParam = req.getParameter("page");
      String pageSizeParam = req.getParameter("rows");
      if (Util.isNullOrEmpty(pageNumberParam)) {
        throw new IllegalArgumentException("'page' parameter is null or empty.");
      }
      if (Util.isNullOrEmpty(pageSizeParam)) {
        throw new IllegalArgumentException("'rows' parameter is null or empty.");
      }
      int pageNumber = Integer.parseInt(pageNumberParam);
      int pageSize = Integer.parseInt(pageSizeParam);
      
      List<Assignment> assignments = getFilteredAssignments(sortColumn, sortOrder, searchColumn,
              searchString, searchType);

      // Total number of results after search filtering, but before paging.
      int totalResults = assignments.size();
      
      // Total number of pages is equal to the integer greater than the total divided by page size.
      // If there are 34 total results and a page size of 5, total pages is 7.
      int totalPages = (int) Math.ceil((double) totalResults / (double) pageSize);
      
      List<Assignment> pageOfAssignments = pageResults(assignments, pageNumber, pageSize);

      String jsonResponse = generateJSONResponse(pageOfAssignments, totalPages, pageNumber,
              totalResults);

      resp.setContentType("text/javascript");
      resp.getWriter().println(jsonResponse);
    } catch (JSONException e) {
      log.log(Level.WARNING, "", e);
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
    } catch (IllegalArgumentException e) {
      log.log(Level.WARNING, "", e);
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
    }
  }
}
