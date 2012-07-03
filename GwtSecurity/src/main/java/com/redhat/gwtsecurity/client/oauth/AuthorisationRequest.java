package com.redhat.gwtsecurity.client.oauth;

import static com.redhat.gwtsecurity.client.oauth.Constants.*;

/**
 * Includes code from the gwt-oauth2-0.2-alpha library found at http://code.google.com/p/gwt-oauth2/
 * <p/>
 * Represents a request for authentication to an OAuth 2.0 provider server, using an OpenId provider for authentication.
 */

public class AuthorisationRequest {
    private final String authUrl;
    private final String tokenUrl;
    private final String clientId;
    private String[] scopes;
    private String openIdProvider;
    private String scopeDelimiter = " ";  // Default delimiter

    /**
     * @param authUrl     URL of the OAuth 2.0 provider server
     * @param clientId    Your application's unique client ID
     */
    public AuthorisationRequest(String authUrl, String tokenUrl, String clientId, String openIdProvider) {
        this.authUrl = authUrl;
        this.tokenUrl = tokenUrl;
        this.clientId = clientId;
        this.openIdProvider = openIdProvider;
    }

    /**
     * Set some OAuth 2.0 scopes to request access to.
     */
    public AuthorisationRequest withScopes(String... scopes) {
        this.scopes = scopes;
        return this;
    }

    public String getTokenUrl() {
        return tokenUrl;
    }

    public String getClientId() {
        return clientId;
    }

    /**
     * Since some OAuth providers expect multiple scopes to be delimited with
     * spaces (conforming with spec), or spaces, or plus signs, you can set the
     * scope delimiter here that will be used for this AuthorisationRequest.
     *
     * <p>
     * By default, this will be a single space, in conformance with the latest
     * draft of the OAuth 2.0 spec.
     * </p>
     */

    public AuthorisationRequest withScopeDelimiter(String scopeDelimiter) {
        this.scopeDelimiter = scopeDelimiter;
        return this;
    }

    /**
     * Returns a URL representation of this request, appending the client ID and
     * scopes to the original authUrl.
     */
    String toLoginUrl(Authoriser.UrlCodex urlCodex) {
        return new StringBuilder(authUrl)
                .append(authUrl.contains(QUERY_STRING_MARKER) ? PARAMETER_SEPARATOR : QUERY_STRING_MARKER)
                .append(CLIENT_ID).append(KEY_VALUE_SEPARATOR).append(urlCodex.encode(clientId))
                .append(PARAMETER_SEPARATOR).append(RESPONSE_TYPE).append(KEY_VALUE_SEPARATOR).append(TOKEN)
                .append(PARAMETER_SEPARATOR).append(SCOPE).append(KEY_VALUE_SEPARATOR).append(scopesToString(urlCodex))
                .append(PARAMETER_SEPARATOR).append(PROVIDER).append(KEY_VALUE_SEPARATOR).append(urlCodex.encode(openIdProvider))
                .toString();
    }

    /**
     * Returns a unique representation of this request for use as a cookie name.
     */
    String asString() {
        //TODO should we add a user identity to this?
        return clientId + SEPARATOR + scopesToString(null);
    }

    /**
     * Returns a comma-delimited list of scopes.
     *
     * These scopes will be URL-encoded if the given codex is not null.
     */
    private String scopesToString(Authoriser.UrlCodex urlCodex) {
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
