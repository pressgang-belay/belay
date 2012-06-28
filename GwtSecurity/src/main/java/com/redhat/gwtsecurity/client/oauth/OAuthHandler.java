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
    private TokenRequest lastTokenRequest;
    private OAuthRequest lastOAuthRequest;
    private Callback<String, Throwable> lastCallback;
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
        AUTH.authorise(request, callback);
    }

    public double expiresIn(AuthorisationRequest req) {
        return AUTH.expiresIn(req);
    }

    /**
     * Request a resource from an OAuth 2.0 provider, refreshing authentication if necessary.
     *
     * @param request Request for resource.
     * @param callback Callback for when access has been granted.
     */
    public void makeRequest(OAuthRequest request, final Callback<String, Throwable> callback) {
    }

    /**
     * Clears all tokens stored by this class.
     */
    public void clearAllTokens() {
        AUTH.clearAllTokens();
    }
}
