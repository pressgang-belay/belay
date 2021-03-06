package org.jboss.pressgang.belay.oauth2.gwt.client;

/**
 * Includes code from the AuthRequest class in the gwt-oauth2-0.2-alpha library (http://code.google.com/p/gwt-oauth2/),
 * written by Jason Hall. Library code has been modified.
 * This code is licensed under Apache License Version 2.0 (http://www.apache.org/licenses/LICENSE-2.0).
 * <p/>
 * Represents a request for authentication to an OAuth 2.0 provider server, using an OpenId provider for authentication.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
public class AuthorizationRequest {
    private final String authUrl;
    private final String clientId;
    private String[] scopes;
    private boolean forceNewRequest;
    private String scopeDelimiter = " ";  // Default delimiter

    /**
     * @param authUrl  URL of the OAuth 2.0 provider server
     * @param clientId Your application's unique client ID
     */
    public AuthorizationRequest(String authUrl, String clientId) {
        this.authUrl = authUrl;
        this.clientId = clientId;
        this.forceNewRequest = false;
    }

    /**
     * Set some OAuth 2.0 scopes to request access to.
     */
    public AuthorizationRequest withScopes(String... scopes) {
        this.scopes = scopes;
        return this;
    }

    /**
     * Ensure request will go to OAuth provider, regardless of whether or not a valid
     * token for this request exists. Defaults to false if not set and resets to default
     * after an authorization attempt, regardless of the outcome.
     */
    public AuthorizationRequest forceNewRequest(boolean forceNewRequest) {
        this.forceNewRequest = forceNewRequest;
        return this;
    }

    public boolean isForceNewRequest() {
        return forceNewRequest;
    }

    /**
     * Since some OAuth providers expect multiple scopes to be delimited with
     * spaces (conforming with spec), or spaces, or plus signs, you can set the
     * scope delimiter here that will be used for this AuthorizationRequest.
     * <p/>
     * By default, this will be a single space, in conformance with the latest
     * draft of the OAuth 2.0 spec.
     */

    public AuthorizationRequest withScopeDelimiter(String scopeDelimiter) {
        this.scopeDelimiter = scopeDelimiter;
        return this;
    }

    /**
     * Returns a URL representation of this request, appending the client ID, scopes and, if provided,
     * OpenID provider to the original authUrl.
     */
    String toAuthUrl(Authorizer.UrlCodex urlCodex) {
        return new StringBuilder(authUrl)
                .append(authUrl.contains(Constants.QUERY_STRING_MARKER) ? Constants.PARAMETER_SEPARATOR : Constants.QUERY_STRING_MARKER)
                .append(Constants.CLIENT_ID).append(Constants.KEY_VALUE_SEPARATOR).append(urlCodex.encode(clientId))
                .append(Constants.PARAMETER_SEPARATOR).append(Constants.RESPONSE_TYPE).append(Constants.KEY_VALUE_SEPARATOR).append(Constants.TOKEN)
                .append(Constants.PARAMETER_SEPARATOR).append(Constants.SCOPE).append(Constants.KEY_VALUE_SEPARATOR).append(scopesToString(urlCodex))
                .toString();
    }

    /**
     * Returns a unique representation of this request for use as a cookie name.
     */
    String asString() {
        return clientId + Constants.SEPARATOR + scopesToString(null);
    }

    /**
     * Returns a comma-delimited list of scopes.
     * <p/>
     * These scopes will be URL-encoded if the given codex is not null.
     */
    private String scopesToString(Authorizer.UrlCodex urlCodex) {
        if (scopes == null || scopes.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        boolean needsSeparator = false;
        for (String scope : scopes) {
            if (needsSeparator) {
                sb.append(scopeDelimiter);
            }
            needsSeparator = true;

            // Use the URL codex to encode each scope, if provided.
            sb.append(urlCodex == null ? scope : urlCodex.encode(scope));
        }
        return sb.toString();
    }
}
