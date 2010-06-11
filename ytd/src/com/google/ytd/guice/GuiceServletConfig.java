package com.google.ytd.guice;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;
import com.google.ytd.embed.AuthSubHandler;
import com.google.ytd.embed.GetUploadToken;
import com.google.ytd.embed.LogoutHandler;
import com.google.ytd.embed.SubmitExistingVideo;
import com.google.ytd.embed.UploadResponseHandler;
import com.google.ytd.jsonrpc.JsonRpcProcessor;
import com.google.ytd.mobile.MobileAuthSub;
import com.google.ytd.mobile.PersistMobileSubmission;
import com.google.ytd.picasa.PersistPicasaAuthSubToken;
import com.google.ytd.tasks.CreateAlbum;
import com.google.ytd.tasks.MoveToPicasa;
import com.google.ytd.youtube.PersistAuthSubToken;
import com.google.ytd.youtube.VideoDownloadRedirect;

public class GuiceServletConfig extends GuiceServletContextListener {

  @Override
  protected Injector getInjector() {
    ServletModule servletModule = new ServletModule() {
      @Override
      protected void configureServlets() {
        // Single entry point for all jsonrpc requests
        serve("/jsonrpc").with(JsonRpcProcessor.class);
        
        // Video download endpoint
        serve("/admin/VideoDownloadRedirect").with(VideoDownloadRedirect.class);
        
        // Frontend jsp embed endpoint
        serve("/embed").with(EmbedJspForwarder.class);
        serve("/logout").with(LogoutHandler.class);
        serve("/UploadResponseHandler").with(UploadResponseHandler.class);
        serve("/GetUploadToken").with(GetUploadToken.class);
        serve("/AuthsubHandler").with(AuthSubHandler.class);
        serve("/LogoutHandler").with(LogoutHandler.class);
        serve("/SubmitExistingVideo").with(SubmitExistingVideo.class);
        serve("/admin/PersistAuthSubToken").with(PersistAuthSubToken.class);
        serve("/admin/PersistPicasaAuthSubToken").with(PersistPicasaAuthSubToken.class);
        serve("/tasks/CreateAlbum").with(CreateAlbum.class);
        serve("/tasks/MoveToPicasa").with(MoveToPicasa.class);

        // Map mobile servlet handlers
        String mobileDir = "/mobile";
        serve(mobileDir + "/MobileAuthSub").with(MobileAuthSub.class);
        serve(mobileDir + "/PersistMobileSubmission").with(PersistMobileSubmission.class);
      }
    };

    return Guice.createInjector(Stage.DEVELOPMENT, servletModule, new ProductionModule());
  }
}
