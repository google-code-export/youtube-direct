package com.google.yaw;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gdata.client.http.AuthSubUtil;
import com.google.gdata.util.AuthenticationException;
import com.google.yaw.model.UserSession;

public class Authenticator {

	private HttpServletRequest request = null;
	private HttpServletResponse response = null;

	private static final String SCOPE = "http://gdata.youtube.com";
	private static final String AUTHSUB_HANDLER = "/AuthsubHandler";

	private UserSession userSession = null;

	private static final Logger log = Logger.getLogger(Authenticator.class.getName());

	public Authenticator(HttpServletRequest req, HttpServletResponse resp) {
		this.request = req;
		this.response = resp;

		this.userSession = UserSessionManager.getUserSession(request);

		String assignmentId = request.getParameter("assignmentId");
		String articleUrl = request.getParameter("articleUrl");

		String selfUrl = Util.getSelfUrl(request);

		if (userSession == null) {
			userSession = new UserSession();
			userSession = UserSessionManager.save(userSession);
			// stick the session id as cookie
			UserSessionManager.sendSessionIdCookie(userSession.getId(), response);
		}

		userSession.setAssignmentId(assignmentId);
		userSession.setArticleUrl(articleUrl);
		userSession.setSelfUrl(selfUrl);
		userSession = UserSessionManager.save(userSession);

		String authSubToken = userSession.getAuthSubToken();

		if (authSubToken != null) {
			// check for bad token
			if (!isTokenValid(authSubToken)) {
				log.finer(String.format("AuthSub token '%s' is invalid. Creating new session.",
						authSubToken));

				authSubToken = null;

				UserSessionManager.delete(userSession);

				// replace with new session

				userSession = new UserSession();
				userSession.setAssignmentId(assignmentId);
				userSession.setArticleUrl(articleUrl);
				userSession.setSelfUrl(selfUrl);
				userSession = UserSessionManager.save(userSession);

				// stick the session id as cookie
				UserSessionManager.sendSessionIdCookie(userSession.getId(), response);
			} else {
				// good token
				log.finest(String.format("Reusing cached AuthSub token '%s'.", authSubToken));
			}
		}
	}

	public boolean isTokenValid(String token) {
		try {
			AuthSubUtil.getTokenInfo(token, null);
		} catch (AuthenticationException e) {
			return false;
		} catch (IOException e) {
			return false;
		} catch (GeneralSecurityException e) {
			return false;
		}
		return true;
	}

	public boolean isLoggedIn() {
		boolean isLoggedIn = false;

		String authSubToken = userSession.getAuthSubToken();

		if (authSubToken != null && isTokenValid(authSubToken)) {
			isLoggedIn = true;
		}

		return isLoggedIn;
	}

	public UserSession getUserSession() {
		return this.userSession;
	}

	public String getLogInUrl() {
		String articleUrl = userSession.getArticleUrl();

		StringBuffer nextUrl = new StringBuffer();
		nextUrl.append(request.getScheme());
		nextUrl.append("://");
		nextUrl.append(request.getServerName());
		if (request.getServerPort() != 80) {
			nextUrl.append(":").append(request.getServerPort());
		}
		nextUrl.append(AUTHSUB_HANDLER);
		nextUrl.append("?articleUrl=");
		nextUrl.append(articleUrl);

		return AuthSubUtil.getRequestUrl(nextUrl.toString(), SCOPE, false, true);
	}

	public String getLogOutUrl() {
		return "/LogoutHandler";
	}

}
