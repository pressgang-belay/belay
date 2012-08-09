package org.jboss.pressgangccms.oauth2.gwt.client.oauth;

import com.google.gwt.core.client.Callback;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Window;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.jboss.pressgangccms.oauth2.gwt.client.oauth.Common.AUTHORISATION_HEADER;
import static org.jboss.pressgangccms.oauth2.gwt.client.oauth.Common.EXPIRES_IN;
import static org.jboss.pressgangccms.oauth2.gwt.client.oauth.Common.OAUTH_HEADER_NAME;

/**
 * Provides methods to manage OAuth login and subsequent requests.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
public class OAuthHandler {

    private AuthorisationRequest lastAuthRequest;
    private final static Authoriser AUTH = Authoriser.get();

    public static OAuthHandler get() {
        return new OAuthHandler();
    }

    private OAuthHandler() {
    }

    /**
     * Authenticate with an OAuth 2.0 provider.
     *
     * @param request  Request for authentication.
     * @param callback Callback for when access has been granted.
     */
    public void login(AuthorisationRequest request, final Callback<String, Throwable> callback) {
        this.lastAuthRequest = request;
        AUTH.authorise(request, callback);
    }

    /**
     * Request a resource from an OAuth 2.0 provider, refreshing authentication if necessary.
     *
     * @param request  OAuthRequest for resource.
     * @param callback RequestCallback defining actions when request succeeds or fails.
     */
    public void sendRequest(final OAuthRequest request, final RequestCallback callback) {
        if (lastAuthRequest == null) {
            callback.onError(null, new RuntimeException("You must log in before making requests"));
        }

        final AuthorisationRequest lastAuthorisationRequest = lastAuthRequest;

        // Retrieve token
        AUTH.authorise(lastAuthorisationRequest, new Callback<String, Throwable>() {
            @Override
            public void onFailure(Throwable reason) {
                callback.onError(null, new RuntimeException("Could not obtain request authorisation"));
            }

            @Override
            public void onSuccess(String token) {
                doOAuthRequest(request, callback, token, lastAuthorisationRequest);
            }
        });
    }

    /**
     * Clears all stored tokens.
     */
    public void clearAllTokens() {
        AUTH.clearAllTokens();
    }

    public String encodeUrl(String url) {
        return AUTH.encodeUrl(url);
    }

    private void doOAuthRequest(final OAuthRequest request, final RequestCallback callback, final String token,
                                final AuthorisationRequest lastAuthorisationRequest) {
        try {
            request.sendRequest(token, callback, this, lastAuthorisationRequest);
        } catch (RequestException e) {
            callback.onError(null, e);
        }
    }

    void processOAuthRequestResponse(final Request request, final Response response, final RequestCallback callback,
                                     final AuthorisationRequest authorisation) {
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
        Authoriser.TokenInfo tokenInfo = AUTH.getToken(lastAuthorisationRequest);
        tokenInfo.expires = AUTH.convertExpiresInFromSeconds(expiresIn);
        AUTH.setToken(lastAuthorisationRequest, tokenInfo);
    }
}
