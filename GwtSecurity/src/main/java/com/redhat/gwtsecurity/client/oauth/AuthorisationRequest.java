package com.redhat.gwtsecurity.client.oauth;

import com.google.gwt.http.client.RequestBuilder;

import static com.redhat.gwtsecurity.client.oauth.Authoriser.SEPARATOR;

/**
 * Includes code from the gwt-oauth2-0.2-alpha library found at http://code.google.com/p/gwt-oauth2/
 * <p/>
 * Represents a request for authentication to an OAuth 2.0 provider server.
 */

public class AuthorisationRequest {
    private final String authUrl;
    private final String clientId;
    private String[] scopes;
    private String provider;
    private String scopeDelimiter = " ";
    private final static String COMMA = ",";
    private final static String SPACE = " ";
    private static final String QUERY_STRING_MARKER = "?";
    private static final String PARAMETER_SEPARATOR = "&";
    private static final String NAME_VALUE_SEPARATOR = "=";
    private static final String AUTHORISATION_HEADER = "Authorization";
    private static final String AUTH_SCHEME = "OAuth";
    private static final String TOKEN = "token";
    private static final String CLIENT_ID = "client_id";
    private static final String RESPONSE_TYPE = "response_type";
    private static final String SCOPE = "scope";
    private static final String REDIRECT_URI = "redirect_uri";
    private static final String PROVIDER = "provider";

    /**
     * @param authUrl     URL of the OAuth 2.0 provider server
     * @param clientId    Your application's unique client ID
     */
    public AuthorisationRequest(String authUrl, String clientId, String openIdProvider) {
        this.authUrl = authUrl;
        this.clientId = clientId;
        this.provider = openIdProvider;
    }

    /**
     * Set some OAuth 2.0 scopes to request access to.
     */
    public AuthorisationRequest withScopes(String... scopes) {
        this.scopes = scopes;
        return this;
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
     * Returns a RequestBuilder representation of this auth request, with the
     * client ID and scopes encoded as headers.
     *
     * @param httpMethod The HTTP method to set on the request
     */
    RequestBuilder build(RequestBuilder.Method httpMethod, Authoriser.UrlCodex urlCodex, String redirectUri) {
        RequestBuilder builder = new RequestBuilder(httpMethod, this.authUrl);
        builder.setHeader(AUTHORISATION_HEADER, encodeOAuthParamString(urlCodex, redirectUri));
        return builder;
    }

    /**
     * Returns a URL representation of this request, appending the client ID and
     * scopes to the original authUrl.
     */
    String toUrl(Authoriser.UrlCodex urlCodex) {
        return new StringBuilder(authUrl)
                .append(authUrl.contains(QUERY_STRING_MARKER) ? PARAMETER_SEPARATOR : QUERY_STRING_MARKER)
                .append(CLIENT_ID).append(NAME_VALUE_SEPARATOR).append(urlCodex.encode(clientId))
                .append(PARAMETER_SEPARATOR).append(RESPONSE_TYPE).append(NAME_VALUE_SEPARATOR).append(TOKEN)
                .append(PARAMETER_SEPARATOR).append(SCOPE).append(NAME_VALUE_SEPARATOR).append(scopesToString(urlCodex))
                .append(PARAMETER_SEPARATOR).append(PROVIDER).append(NAME_VALUE_SEPARATOR).append(urlCodex.encode(provider))
                .toString();
    }

    /**
     * Returns encoded parameters of this request with an OAuth header.
     */
    String encodeOAuthParamString(Authoriser.UrlCodex urlCodex, String redirectUri) {
        return new StringBuilder(AUTH_SCHEME).append(SPACE)
                .append(CLIENT_ID).append(NAME_VALUE_SEPARATOR).append(urlCodex.encode(clientId))
                .append(REDIRECT_URI).append(NAME_VALUE_SEPARATOR).append(urlCodex.encode(redirectUri))
                .append(PARAMETER_SEPARATOR).append(RESPONSE_TYPE).append(NAME_VALUE_SEPARATOR).append(TOKEN)
                .append(PARAMETER_SEPARATOR).append(SCOPE).append(NAME_VALUE_SEPARATOR).append(scopesToString(urlCodex))
                .toString();
    }

    /**
     * Returns a unique representation of this request for use as a cookie name.
     */
    String asString() {
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

    //TODO Is this needed?
    /**
     * Returns an {@link AuthorisationRequest} represented by the string serialization.
     */
    static AuthorisationRequest fromString(String str) {
        String[] parts = str.split(SEPARATOR);
        String clientId = parts[0];
        String[] scopes = parts.length == 2 ? parts[1].split(COMMA) : new String[0];
        AuthorisationRequest req = new AuthorisationRequest("", clientId, "").withScopes(scopes);
        return req;
    }
}
