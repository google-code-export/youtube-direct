package com.google.ytd.command;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.logging.Logger;

import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;

public class OpenChannelConnection extends Command {
  private static final Logger LOG = Logger.getLogger(OpenChannelConnection.class.getName());
  private ChannelService channelService = ChannelServiceFactory.getChannelService();

  @Override
  public JSONObject execute() throws JSONException {
    String channelId = String.valueOf(System.currentTimeMillis());
    // There's a not-very-well-documented limit on 64 characters for the channel id.
    if (channelId.length() >= 64) {
      channelId = channelId.substring(0, 63);
    }

    String token = channelService.createChannel(channelId);
    LOG.info(String.format("Channel id is '%s' and token is '%s'.", channelId, token));
    
    JSONObject json = new JSONObject();
    json.put("token", token);
    json.put("channelId", channelId);
    return json;
  }
}