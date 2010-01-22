package com.google.ytd.jsonrpc;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.ytd.command.Command;
import com.google.ytd.command.GetVideoSubmissions;

@Singleton
public class CommandDirectory {
  private static final Logger LOG = Logger.getLogger(CommandDirectory.class.getName());
  private Map<String, Class<? extends Command>> map = null;
  private Injector injector = null;

  @Inject
  public CommandDirectory(Injector injector) {
    this.injector = injector;
    map = new HashMap<String, Class<? extends Command>>();
    initCommandMapping();
  }

  private void initCommandMapping() {
    add("ytd.getSubmissions", GetVideoSubmissions.class);
  }

  public void add(String method, Class<? extends Command> command) {
    map.put(method, command);
  }

  public Command getCommand(String method, Map<String, String> params)
      throws IllegalArgumentException {
    Command command = null;
    if (map.containsKey(method)) {
      command = injector.getInstance(map.get(method));
      command.setParams(params);
    } else {
      throw new IllegalArgumentException(String.format("%s is not a valid method", method));
    }
    return command;
  }
}
