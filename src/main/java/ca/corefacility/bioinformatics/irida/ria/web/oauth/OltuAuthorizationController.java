package ca.corefacility.bioinformatics.irida.ria.web.oauth;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.UriBuilder;

import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthAuthzResponse;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.ResponseType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import ca.corefacility.bioinformatics.irida.model.RemoteAPI;
import ca.corefacility.bioinformatics.irida.service.RemoteAPIService;
import ca.corefacility.bioinformatics.irida.service.RemoteAPITokenService;

/**
 * Controller for handling OAuth2 authorizations
 */
@Controller
public class OltuAuthorizationController {
	private static final Logger logger = LoggerFactory.getLogger(OltuAuthorizationController.class);

	@Value("${server.base.url}")
	private String serverBase;

	public static final String TOKEN_ENDPOINT = "/api/oauth/authorization/token";

	private final RemoteAPITokenService tokenService;
	private final RemoteAPIService remoteAPIService;

	@Autowired
	public OltuAuthorizationController(RemoteAPITokenService tokenService, RemoteAPIService remoteAPIService) {
		this.tokenService = tokenService;
		this.remoteAPIService = remoteAPIService;
	}

	/**
	 * Begin authentication procedure by redirecting to remote authorization location
	 *
	 * @param session   The current {@link HttpSession}
	 * @param remoteAPI The API we need to authenticate with
	 * @param redirect  The location to redirect back to after authentication is complete
	 * @return A ModelAndView beginning the authentication procedure
	 * @throws OAuthSystemException if we can't read from the authorization server.
	 */
	public String authenticate(HttpSession session, RemoteAPI remoteAPI, String redirect) throws OAuthSystemException {
		// get the URI for the remote service we'll be requesting from
		String serviceURI = remoteAPI.getServiceURI();

		// build the authorization path
		URI serviceAuthLocation = UriBuilder.fromUri(serviceURI).path("oauth").path("authorize").build();

		logger.debug("Authenticating for service: " + remoteAPI);
		logger.debug("Redirect after authentication: " + redirect);

		// build a redirect URI to redirect to after auth flow is completed
		String tokenRedirect = buildRedirectURI();

		// build state object which is used to extract the authCode to the correct remoteAPI
		String stateUuid = UUID.randomUUID().toString();
		Map<String, String> stateMap = new HashMap<String, String>();
		stateMap.put("apiId", remoteAPI.getId().toString());
		stateMap.put("redirect", redirect);
		session.setAttribute(stateUuid, stateMap);

		// build the redirect query to request an authorization code from the
		// remote API
		OAuthClientRequest request = OAuthClientRequest.authorizationLocation(serviceAuthLocation.toString())
				.setClientId(remoteAPI.getClientId())
				.setRedirectURI(tokenRedirect)
				.setResponseType(ResponseType.CODE.toString())
				.setScope("read")
				.setState(stateUuid)
				.buildQueryMessage();

		String locURI = request.getLocationUri();
		logger.trace("Authorization request location: " + locURI);

		return "redirect:" + locURI;
	}

	/**
	 * Receive the OAuth2 authorization code and request an OAuth2 token
	 *
	 * @param request  The incoming request
	 * @param response The response to redirect
	 * @param state    The state param which contains a map including apiId and redirect
	 * @return A ModelAndView redirecting back to the resource that was requested
	 * @throws OAuthSystemException  if we can't get an access token for the current request.
	 * @throws OAuthProblemException if we can't get a response from the authorization server
	 */
	@RequestMapping(TOKEN_ENDPOINT)
	public String getTokenFromAuthCode(HttpServletRequest request, HttpServletResponse response,
			@RequestParam("state") String state) throws OAuthSystemException, OAuthProblemException {

		// Get the OAuth2 auth code
		OAuthAuthzResponse oar = OAuthAuthzResponse.oauthCodeAuthzResponse(request);
		String code = oar.getCode();
		logger.trace("Received auth code: " + code);

		HttpSession session = request.getSession();

		Map<String, String> stateMap = (Map<String, String>) session.getAttribute(state);

		Long apiId = Long.parseLong(stateMap.get("apiId"));
		String redirect = stateMap.get("redirect");

		// Build the redirect URI to request a token from
		String tokenRedirect = buildRedirectURI();

		// Read the RemoteAPI from the RemoteAPIService and get the base URI
		RemoteAPI remoteAPI = remoteAPIService.read(apiId);

		tokenService.createTokenFromAuthCode(code, remoteAPI, tokenRedirect);

		// redirect the response back to the requested resource
		return "redirect:" + redirect;
	}

	/**
	 * Build the redirect URI for the token page with the API and resource page
	 *
	 * @return the redirect uri
	 */
	private String buildRedirectURI() {

		URI build = UriBuilder.fromUri(serverBase).path(TOKEN_ENDPOINT).build();

		return build.toString();
	}

	/**
	 * Set the base URL of this server
	 *
	 * @param serverBase the base url of the server.
	 */
	public void setServerBase(String serverBase) {
		this.serverBase = serverBase;
	}
}
