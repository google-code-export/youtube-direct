package com.google.ytd.jsonrpc;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.ytd.command.Command;
import com.google.ytd.dao.SubmissionManager;
import com.google.ytd.model.VideoSubmission;

public class BasicCommandTest{
  @Before
  public void setUp() {
  }

  @Test
  public void testGetSubmissions() throws JSONException {
    AbstractModule testModule = new AbstractModule() {
      @Override
      protected void configure() {
        final VideoSubmission videoSubmission = new VideoSubmission(1l);
        videoSubmission.setArticleUrl("blah");
        final List<VideoSubmission> submissions = new ArrayList<VideoSubmission>();
        submissions.add(videoSubmission);

        JUnit4Mockery mockery = new JUnit4Mockery();
        final SubmissionManager manager = mockery.mock(SubmissionManager.class);
        mockery.checking(new Expectations() {{
          oneOf(manager).getSubmissions(with("created"), with("desc"), with("all"));
          will(returnValue(submissions));
        }});

        this.bind(SubmissionManager.class).toInstance(manager);
      }
    };

    Injector injector = Guice.createInjector(testModule);
    CommandDirectory commandDirectory = new CommandDirectory(injector);

    String method = "ytd.getSubmissions";
    Map<String,String> params = new HashMap<String,String>();
    params.put("sortBy", "created");
    params.put("sortOrder", "desc");
    params.put("filterType", "all");
    params.put("pageIndex", "1");
    params.put("pageSize", "10");
    Command command = commandDirectory.getCommand(method, params);
    JSONObject response = command.execute();
    assertNotNull("JSONObject is null", response);
    assertTrue("result length is zero", response.getInt("totalPages") > 0);
  }
}
