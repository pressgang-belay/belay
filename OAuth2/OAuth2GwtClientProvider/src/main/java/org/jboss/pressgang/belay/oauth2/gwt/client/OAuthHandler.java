package org.jboss.pressgang.belay.oauth2.gwt.client;

import com.google.gwt.core.client.Callback;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;

import static org.jboss.pressgang.belay.oauth2.gwt.client.Constants.AUTHORIZATION_HEADER;
import static org.jboss.pressgang.belay.oauth2.gwt.client.Constants.EXPIRES_IN;
import static org.jboss.pressgang.belay.oauth2.gwt.client.Constants.OAUTH_HEADER_NAME;

/**
 * Provides methods to manage OAuth authorization and subsequent requests.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
public class OAuthHandler {

    AuthorizationRequest lastSuccessfulAuthRequest;
    private String lastTokenResult;
    private static Authorizer auth;

    public static OAuthHandler get() {
        auth = Authorizer.get();
        return new OAuthHandler();
    }

    OAuthHandler() {
    }

    OAuthHandler(Authorizer authorizer) {
        auth = authorizer;
    }

    /**
     * Gain end-user authorization with an OAuth 2.0 provider.
     *
     * @param request  Request for authentication.
     * @param callback Callback for when access has been granted.
     */
    public void sendAuthRequest(final AuthorizationRequest request, final Callback<String, Throwable> callback) {
        auth.authorize(request, new Callback<String, Throwable>() {
            @Override
            public void onFailure(Throwable reason) {
                callback.onFailure(reason);
            }

            @Override
            public void onSuccess(String result) {
                lastTokenResult = result;
                lastSuccessfulAuthRequest = request;
                callback.onSuccess(result);
            }
        });
    }

    /**
     * Request a resource from an OAuth 2.0 provider, refreshing authentication if necessary.
     *
     * @param request  OAuthRequest for resource.
     * @param callback RequestCallback defining actions when request succeeds or fails.
     */
    public void sendRequest(final OAuthRequest request, final RequestCallback callback) {
        if (lastSuccessfulAuthRequest == null) {
            callback.onError(null, new RuntimeException("You must be authorized before making requests"));
        }

        final AuthorizationRequest lastAuthorizationRequest = lastSuccessfulAuthRequest;

        // Retrieve token or prompt authorization
        auth.authorize(lastAuthorizationRequest, new Callback<String, Throwable>() {
            @Override
            public void onFailure(Throwable reason) {
                callback.onError(null, new RuntimeException("Could not obtain request authorization"));
            }

            @Override
            public void onSuccess(String token) {
                lastTokenResult = token;
                doOAuthRequest(request, token, lastAuthorizationRequest, callback);
            }
        });
    }

    /**
     * Request a resource from an OAuth 2.0 provider using the credentials from a specific AuthorizationRequest.
     *
     * @param request     OAuthRequest for resource.
     * @param callback    RequestCallback defining actions when request succeeds or fails.
     * @param authRequest AuthorizationRequest to use for authorization credentials.
     */
    public void sendRequestWithSpecificAuthorization(final OAuthRequest request, final RequestCallback callback,
                                                     final AuthorizationRequest authRequest) {
        String token = getTokenForRequest(authRequest);
        if (token == null) {
            callback.onError(null, new RuntimeException("Invalid token"));
        }
        doOAuthRequest(request, token, authRequest, callback);
    }

    /**
     * Clears all stored tokens.
     */
    public void clearAllTokens() {
        auth.clearAllTokens();
    }

    /**
     * Get the last token returned after a successful authorization attempt. Will be null if no such attempt
     * has been made.
     *
     * @return OAuth2 access token String.
     */
    public String getLastTokenResult() {
        return lastTokenResult;
    }

    /**
     * Get the last successful authorization request. Will be null if there has been no successful requests.
     *
     * @return OAuth2 access token String.
     */
    public AuthorizationRequest getLastSuccessfulAuthorizationRequest() {
        return lastSuccessfulAuthRequest;
    }

    /**
     * Get the last token returned after a successful authorization attempt using the given request parameter.
     * Will be null if no such request result is found.
     *
     * @param request The AuthorizationRequest to query with.
     * @return OAuth2 access token String.
     */
    public String getTokenForRequest(AuthorizationRequest request) {
        Authorizer.TokenInfo tokenInfo = auth.getToken(request);
        if (tokenInfo != null) {
            return tokenInfo.accessToken;
        }
        return null;
    }

    public String encodeUrl(String url) {
        return auth.encodeUrl(url);
    }

    private void doOAuthRequest(final OAuthRequest request, final String token,
                                final AuthorizationRequest authorization, final RequestCallback callback) {
        try {
            request.sendRequest(token, this, authorization, callback);
        } catch (RequestException e) {
            callback.onError(null, e);
        }
    }

    void processOAuthRequestResponse(final Request request, final Response response, final AuthorizationRequest authorization,
                                     final RequestCallback callback) {
        if (response.getHeader(AUTHORIZATION_HEADER) != null
                && response.getHeader(AUTHORIZATION_HEADER).startsWith(OAUTH_HEADER_NAME)) {
            // Get header and remove leading OAuth header name and space
            String authResponse = response.getHeader(AUTHORIZATION_HEADER).substring(OAUTH_HEADER_NAME.length() + 1);
            String expiresIn = null;
            if (authResponse.contains(",")) {
                String[] tokens = authResponse.split(",");
                for (String token : tokens) {
                    String result = checkForExpiresIn(token);
                    if (result != null) {
                        expiresIn = result;
                        break;
                    }
                }
            } else {
                expiresIn = checkForExpiresIn(authResponse);
            }
            if (expiresIn != null) {
                // Cumbersome way of doing this but regular regex did not seem to work and didn't want to bring in
                // whole JavaScript RegExp library
                if (expiresIn.startsWith("\"") || expiresIn.startsWith("'")) {
                    expiresIn = expiresIn.substring(1);
                }
                if (expiresIn.endsWith("\"") || expiresIn.endsWith("'")) {
                    expiresIn = expiresIn.substring(0, expiresIn.length() - 1);
                }
                setNewExpiresIn(expiresIn, authorization);
            }
        }
        callback.onResponseReceived(request, response);
    }

    private String checkForExpiresIn(String str) {
        if (str.startsWith(EXPIRES_IN)) {
            return str.substring(str.indexOf('=') + 1);
        }
        return null;
    }

    private void setNewExpiresIn(final String expiresIn, final AuthorizationRequest lastAuthorizationRequest) {
        Authorizer.TokenInfo tokenInfo = auth.getToken(lastAuthorizationRequest);
        tokenInfo.expires = auth.convertExpiresInFromSeconds(expiresIn);
        auth.setToken(lastAuthorizationRequest, tokenInfo);
    }
}
