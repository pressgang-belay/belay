package org.jboss.pressgangccms.oauth.gwt.client.oauth;

import com.google.gwt.core.client.Callback;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;

/**
 * Provides methods to manage OAuth login and subsequent requests.
 *
 * @author kamiller@redhat.com (Katie Miller)
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

    /**
     * Clears all stored tokens.
     */
    public void clearAllTokens() {
        AUTH.clearAllTokens();
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
}
