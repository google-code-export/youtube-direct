package com.google.yaw;

import java.io.IOException;
import java.security.GeneralSecurityException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gdata.client.http.AuthSubUtil;
import com.google.gdata.util.AuthenticationException;
import com.google.yaw.model.UserSession;

/**
 * Super simple class to handle doing the AuthSub token exchange to upgrade a
 * one-time token into a session token.
 * 
 */
@SuppressWarnings("serial")
public class AuthSubHandler extends HttpServlet {

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException {

		String token = AuthSubUtil.getTokenFromReply(request.getQueryString());

		if (token == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST,
					"No token specified.");
			return;
		}

		try {
			String articleUrl = request.getParameter("articleUrl");
			if (articleUrl != null) {
				String authSubToken = AuthSubUtil.exchangeForSessionToken(
						token, null);

				UserSession userSession = UserSessionManager
						.getUserSession(request);

				if (userSession == null) {
					response.sendError(HttpServletResponse.SC_BAD_REQUEST,
							"No current user session is found.");
					return;
				} else {
					userSession.setAuthSubToken(authSubToken);

					// get YouTube username

					YouTubeApiManager apiManager = new YouTubeApiManager();
					apiManager.setToken(authSubToken);

					String youTubeName = apiManager.getCurrentUsername();

					userSession.setYouTubeName(youTubeName);

					UserSessionManager.save(userSession);
				}

				response.sendRedirect(articleUrl);
			} else {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST,
						"Missing redirection URL");
			}
		} catch (AuthenticationException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"Server rejected one time use token.");
		} catch (GeneralSecurityException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"Security error while retrieving session token.");
		}
		return;
	}

}
