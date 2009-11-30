package com.google.ytd.jsonrpc;

import static org.junit.Assert.assertNotNull;

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
import com.google.ytd.dao.SubmissionManager;
import com.google.ytd.model.VideoSubmission;

public class BasicCommandTest{
  Injector injector = null;
  CommandDirectory commandDirectory = null;

  @Before
  public void setUp() {

    AbstractModule testModule = new AbstractModule() {

      @Override
      protected void configure() {
        final VideoSubmission videoSubmission = new VideoSubmission(1l);
        videoSubmission.setArticleUrl("blah");
        List<VideoSubmission> submissions = new ArrayList<VideoSubmission>();
        submissions.add(videoSubmission);

        JUnit4Mockery mockery = new JUnit4Mockery();
        final SubmissionManager manager = mockery.mock(SubmissionManager.class);
        mockery.checking(new Expectations() {{
          oneOf(manager).getSubmissions(with("created"), with(""), with(""));
          will(returnValue(videoSubmission));
        }});

        this.bind(SubmissionManager.class).toInstance(manager);
      }
    };

    injector = Guice.createInjector(testModule);
    commandDirectory = new CommandDirectory(injector);
  }

  @Test
  public void testGetSubmissions() throws JSONException {
    String method = "ytd.getSubmissions";
    Map<String,String> params = new HashMap<String,String>();
    Command command = commandDirectory.getCommand(method, params);
    JSONObject response = command.execute();
    assertNotNull(response);
  }
}
