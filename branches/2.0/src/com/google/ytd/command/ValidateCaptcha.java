package com.google.ytd.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import com.google.ytd.dao.AdminConfigDao;
import com.google.ytd.util.Util;

@RequestScoped
public class ValidateCaptcha extends Command {
  private static final Logger LOG = Logger.getLogger(NewAssignment.class.getName());

  private static final String CAPTCHA_VALIDATE_URL = "http://api-verify.recaptcha.net/verify";

  @Inject
  private Util util;

  @Inject
  AdminConfigDao adminConfigDao;

  HttpServletRequest request;

  @Inject
  public ValidateCaptcha(HttpServletRequest request, AdminConfigDao adminConfigDao) {
    this.request = request;
    this.adminConfigDao = adminConfigDao;
  }

  @Override
  public JSONObject execute() throws JSONException {
    JSONObject json = new JSONObject();

    String challenge = getParam("challenge");
    String response = getParam("response");
    String remoteIp = request.getRemoteAddr();
    //TODO: Make this a AdminConfig setting
    String privateKey = "6Le99goAAAAAAG8wa3XtBSDSjLi0WO24-38XMbYo";

    if (util.isNullOrEmpty(challenge)) {
      throw new IllegalArgumentException("Missing required param: 'challenge'");
    }

    if (util.isNullOrEmpty(response)) {
      throw new IllegalArgumentException("Missing required param: 'response'");
    }

    try {
      URL url = new URL(CAPTCHA_VALIDATE_URL);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setDoOutput(true);
      connection.setRequestMethod("POST");

      OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
      String postBody = String.format("challenge=%s&response=%s&remoteip=%s&privatekey=%s",
          challenge, response, remoteIp, privateKey);

      LOG.info(String.format("POSTing '%s' to '%s'.", postBody, CAPTCHA_VALIDATE_URL));

      writer.write(postBody);
      writer.close();

      if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection
            .getInputStream()));
        String line = reader.readLine().trim();
        
        // line will be 'true' if this was a success, or 'false' otherwise.
        json.put("result", line);
      } else {
        LOG.warning(String.format("Response code %d returned from %s.", connection
            .getResponseCode(), CAPTCHA_VALIDATE_URL));
      }
    } catch (MalformedURLException e) {
      LOG.log(Level.WARNING, "", e);
    } catch (ProtocolException e) {
      LOG.log(Level.WARNING, "", e);
    } catch (IOException e) {
      LOG.log(Level.WARNING, "", e);
    }

    return json;
  }
}
