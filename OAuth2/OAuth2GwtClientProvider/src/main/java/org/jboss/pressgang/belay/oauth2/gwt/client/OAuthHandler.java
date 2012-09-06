package org.jboss.pressgang.belay.oauth2.gwt.client;

import com.google.gwt.core.client.Callback;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;

import static org.jboss.pressgang.belay.oauth2.gwt.client.Constants.AUTHORISATION_HEADER;
import static org.jboss.pressgang.belay.oauth2.gwt.client.Constants.EXPIRES_IN;
import static org.jboss.pressgang.belay.oauth2.gwt.client.Constants.OAUTH_HEADER_NAME;

/**
 * Provides methods to manage OAuth authorisation and subsequent requests.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
public class OAuthHandler {

    AuthorisationRequest lastAuthRequest;
    private String lastTokenResult;
    private static Authoriser auth;

    public static OAuthHandler get() {
        auth = Authoriser.get();
        return new OAuthHandler();
    }

    OAuthHandler() {
    }

    OAuthHandler(Authoriser authoriser) {
        auth = authoriser;
    }

    /**
     * Gain end-user authorisation with an OAuth 2.0 provider.
     *
     * @param request  Request for authentication.
     * @param callback Callback for when access has been granted.
     */
    public void sendAuthRequest(AuthorisationRequest request, final Callback<String, Throwable> callback) {
        this.lastAuthRequest = request;
        auth.authorise(request, new Callback<String, Throwable>() {
            @Override
            public void onFailure(Throwable reason) {
                callback.onFailure(reason);
            }

            @Override
            public void onSuccess(String result) {
                lastTokenResult = result;
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
        if (lastAuthRequest == null) {
            callback.onError(null, new RuntimeException("You must be authorised before making requests"));
        }

        final AuthorisationRequest lastAuthorisationRequest = lastAuthRequest;

        // Retrieve token or prompt authorisation
        auth.authorise(lastAuthorisationRequest, new Callback<String, Throwable>() {
            @Override
            public void onFailure(Throwable reason) {
                callback.onError(null, new RuntimeException("Could not obtain request authorisation"));
            }

            @Override
            public void onSuccess(String token) {
                lastTokenResult = token;
                doOAuthRequest(request, token, lastAuthorisationRequest, callback);
            }
        });
    }

    /**
     * Clears all stored tokens.
     */
    public void clearAllTokens() {
        auth.clearAllTokens();
    }

    /**
     * Get the last token returned after a successful authorisation attempt. Will be null if no such attempt
     * has been made.
     *
     * @return OAuth2 access token String
     */
    public String getLastTokenResult() {
        return lastTokenResult;
    }

    /**
     * Get the last token returned after a successful authorisation attempt using the given request parameter.
     * Will be null if no such request result is found.
     *
     * @param request The AuthorisationRequest to query with
     * @return OAuth2 access token String
     */
    public String getTokenForRequest(AuthorisationRequest request) {
        Authoriser.TokenInfo tokenInfo = auth.getToken(request);
        if (tokenInfo != null) {
            return tokenInfo.accessToken;
        }
        return null;
    }

    public String encodeUrl(String url) {
        return auth.encodeUrl(url);
    }

    private void doOAuthRequest(final OAuthRequest request, final String token,
                                final AuthorisationRequest lastAuthorisationRequest, final RequestCallback callback) {
        try {
            request.sendRequest(token, this, lastAuthorisationRequest, callback);
        } catch (RequestException e) {
            callback.onError(null, e);
        }
    }

    void processOAuthRequestResponse(final Request request, final Response response, final AuthorisationRequest authorisation,
                                     final RequestCallback callback) {
        if (response.getHeader(AUTHORISATION_HEADER) != null
                && response.getHeader(AUTHORISATION_HEADER).startsWith(OAUTH_HEADER_NAME)) {
            // Get header and remove leading OAuth header name and space
            String authResponse = response.getHeader(AUTHORISATION_HEADER).substring(OAUTH_HEADER_NAME.length() + 1);
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
                setNewExpiresIn(expiresIn, authorisation);
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

    private void setNewExpiresIn(final String expiresIn, final AuthorisationRequest lastAuthorisationRequest) {
        Authoriser.TokenInfo tokenInfo = auth.getToken(lastAuthorisationRequest);
        tokenInfo.expires = auth.convertExpiresInFromSeconds(expiresIn);
        auth.setToken(lastAuthorisationRequest, tokenInfo);
    }
}
