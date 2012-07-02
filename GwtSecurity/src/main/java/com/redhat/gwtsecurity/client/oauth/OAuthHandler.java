package com.redhat.gwtsecurity.client.oauth;

import com.google.gwt.core.client.Callback;
import com.google.gwt.http.client.*;
import com.google.gwt.user.client.Window;

/**
 * Includes code from the gwt-oauth2-0.2-alpha library found at http://code.google.com/p/gwt-oauth2/
 *
 * Provides methods to manage authentication and subsequent requests.
 **/

public class OAuthHandler {

    private AuthorisationRequest lastAuthRequest;
    private final static Authoriser AUTH = Authoriser.get();

    public static final OAuthHandler get() {
        return new OAuthHandler();
    }

    private OAuthHandler() {
    }

    /**
     * Authenticate with an OAuth 2.0 provider.
     *
     * @param request Request for authentication.
     * @param callback Callback for when access has been granted.
     */
    public void login(AuthorisationRequest request, final Callback<String, Throwable> callback) {
        this.lastAuthRequest = request;
        AUTH.authorise(request, callback);
    }

    /**
     * Request a resource from an OAuth 2.0 provider, refreshing authentication if necessary.
     *
     * @param request OAuthRequest for resource.
     * @param callback RequestCallback defining actions when request succeeds or fails.
     */
    public void sendRequest(final OAuthRequest request, final RequestCallback callback) {
        if (lastAuthRequest == null) {
            callback.onError(null, new RuntimeException("You must log in before making requests"));
        }

        // Retrieve token
        AUTH.authorise(lastAuthRequest, new Callback<String, Throwable>() {
            @Override
            public void onFailure(Throwable reason) {
                callback.onError(null, new RuntimeException("Could not obtain request authorisation"));
            }

            @Override
            public void onSuccess(String token) {
                doOAuthRequest(request, callback, token);
            }
        });
    }

    //TODO remove; this is for testing only
    public void doRefresh(final AuthorisationRequest authorisationRequest, final Callback<String, Throwable> callback) {
        AUTH.doRefresh(authorisationRequest, AUTH.getToken(authorisationRequest), callback);
    }

    private void doOAuthRequest(OAuthRequest request, final RequestCallback callback, String token) {
        try {
            request.sendRequest(token, callback);
        } catch (RequestException e) {
            callback.onError(null, e);
        }
    }

    /**
     * Clears all stored tokens.
     */
    public void clearAllTokens() {
        AUTH.clearAllTokens();
    }
}
