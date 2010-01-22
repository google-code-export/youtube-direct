package com.google.ytd.guice;

import java.util.logging.Logger;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;
import com.google.ytd.embed.LogoutHandler;
import com.google.ytd.jsonrpc.JsonRpcProcessor;

public class GuiceServletConfig extends GuiceServletContextListener {
  private static final Logger LOG = Logger.getLogger(GuiceServletConfig.class.getName());

  @Override
  protected Injector getInjector() {
    ServletModule servletModule = new ServletModule() {
      @Override
      protected void configureServlets() {
        // Single entry point for all jsonrpc requests
        serve("/jsonrpc").with(JsonRpcProcessor.class);

        // Frontend jsp embed endpoint
        serve("/embed").with(EmbedJspForwarder.class);
        serve("/logout").with(LogoutHandler.class);
      }
    };

    return Guice.createInjector(Stage.DEVELOPMENT, servletModule, new ProductionModule());
  }

}
