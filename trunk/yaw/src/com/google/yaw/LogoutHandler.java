package com.google.yaw;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gdata.client.http.AuthSubUtil;
import com.google.gdata.util.AuthenticationException;
import com.google.yaw.model.UserSession;

/**
 * Simple servlet to handle logging a user out.
 * 
 */
@SuppressWarnings("serial")
public class LogoutHandler extends HttpServlet {

	private static final Logger log = Logger.getLogger(LogoutHandler.class
			.getName());

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		UserSession userSession = UserSessionManager.getUserSession(req);

		// revoke AuthSub token
		String authSubToken = userSession.getAuthSubToken();
		if (authSubToken != null) {
			try {
				AuthSubUtil.revokeToken(authSubToken, null);
			} catch (AuthenticationException e) {
                log.warning(String.format("Error while revoking AuthSub token '%s': %s",
                                authSubToken, e.toString()));
			} catch (GeneralSecurityException e) {
                log.warning(String.format("Error while revoking AuthSub token '%s': %s",
                                authSubToken, e.toString()));
			}
		}

		// remove local cookie
		UserSessionManager.destroySessionIdCookie(resp);

		// get the original url to redirect
		String redirectUrl = userSession.getSelfUrl();

		// remove the session entry
		UserSessionManager.delete(userSession);

		// send the redirect
		resp.sendRedirect(redirectUrl);

	}
}