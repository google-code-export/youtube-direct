package com.google.ytd.guice;

import java.util.logging.Logger;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;
import com.google.ytd.ApprovedVideoFeed;
import com.google.ytd.admin.DeleteSubmission;
import com.google.ytd.admin.GetAdminConfig;
import com.google.ytd.admin.GetAllAssignments;
import com.google.ytd.admin.GetAllSubmissions;
import com.google.ytd.admin.InsightDownloadRedirect;
import com.google.ytd.admin.NewAssignment;
import com.google.ytd.admin.PersistAdminConfig;
import com.google.ytd.admin.PersistAuthSubToken;
import com.google.ytd.admin.SyncMetadata;
import com.google.ytd.admin.UpdateAssignment;
import com.google.ytd.admin.UpdateSubmission;
import com.google.ytd.admin.VideoDownloadRedirect;
import com.google.ytd.embed.AuthSubHandler;
import com.google.ytd.embed.GetUploadToken;
import com.google.ytd.embed.LogoutHandler;
import com.google.ytd.embed.SubmitExistingVideo;
import com.google.ytd.embed.UploadResponseHandler;
import com.google.ytd.jsonrpc.JsonRpcProcessor;
import com.google.ytd.mobile.MobileAuthSub;
import com.google.ytd.mobile.PersistMobileSubmission;

public class GuiceServletConfig extends GuiceServletContextListener {
	private static final Logger LOG = Logger.getLogger(GuiceServletConfig.class.getName());

	@Override
	protected Injector getInjector() {
		ServletModule servletModule = new ServletModule() {
			@Override
			protected void configureServlets() {
				// TODO(austinchau) Remove V1 binding when fully migrated to V2 jsonrpc
				// style
				initV1Binding();

				// Single entry point for all jsonrpc requests
				serve("/jsonrpc").with(JsonRpcProcessor.class);

				// Frontend jsp embed endpoint
				serve("/embed").with(EmbedJspForwarder.class);
				serve("/logout").with(LogoutHandler.class);
			}

			private void initV1Binding() {
				String adminDir = "/admin";
				String cronDir = "/cron";
				String mobileDir = "/mobile";

				// Map admin servlet handlers
				serve(adminDir + "/GetAllSubmissions").with(GetAllSubmissions.class);
				serve(adminDir + "/PersistAuthSubToken").with(PersistAuthSubToken.class);
				serve(adminDir + "/InsightDownloadRedirect").with(InsightDownloadRedirect.class);
				serve(adminDir + "/VideoDownloadRedirect").with(VideoDownloadRedirect.class);
				serve(adminDir + "/GetAdminConfig").with(GetAdminConfig.class);
				serve(adminDir + "/PersistAdminConfig").with(PersistAdminConfig.class);
				serve(adminDir + "/UpdateSubmission").with(UpdateSubmission.class);
				serve(adminDir + "/DeleteSubmission").with(DeleteSubmission.class);
				serve(adminDir + "/NewAssignment").with(NewAssignment.class);
				serve(adminDir + "/UpdateAssignment").with(UpdateAssignment.class);
				serve(adminDir + "/GetAllAssignments").with(GetAllAssignments.class);
				// serve(adminDir + "/test").with(FullTextIndexer.class);
				// Map cron jobs servlet handlers
				serve(cronDir + "/SyncMetadata").with(SyncMetadata.class);
				// Map mobile servlet handlers
				serve(mobileDir + "/MobileAuthSub").with(MobileAuthSub.class);
				serve(mobileDir + "/PersistMobileSubmission").with(PersistMobileSubmission.class);
				// Map frontend servlet handlers
				serve("/ApprovedVideoFeed").with(ApprovedVideoFeed.class);
				serve("/UploadResponseHandler").with(UploadResponseHandler.class);
				serve("/GetUploadToken").with(GetUploadToken.class);
				serve("/AuthsubHandler").with(AuthSubHandler.class);
				serve("/LogoutHandler").with(LogoutHandler.class);
				serve("/SubmitExistingVideo").with(SubmitExistingVideo.class);
			}

		};

		return Guice.createInjector(Stage.DEVELOPMENT, servletModule, new ProductionModule());
	}

}
