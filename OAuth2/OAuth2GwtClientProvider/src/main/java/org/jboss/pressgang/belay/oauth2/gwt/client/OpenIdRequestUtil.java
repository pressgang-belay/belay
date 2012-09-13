package org.jboss.pressgang.belay.oauth2.gwt.client;

import com.google.gwt.http.client.RequestBuilder;

import static org.jboss.pressgang.belay.oauth2.gwt.client.Constants.*;

/**
 * Provides factory methods to create AuthorisationRequests for use with OAuth2 authorisation servers using OpenID for
 * authentication, such as the PressGang Belay implementation. Also provides factory methods for requests to PressGang
 * Belay user management endpoints. AuthorisationRequests are used with these user management endpoints if they return
 * new authorisation credentials.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
public class OpenIdRequestUtil {

    private static final OAuthHandler oAuthHandler = OAuthHandler.get();

    /**
     * @param authUrl  URL of the OAuth 2.0 Authorisation Server's auth endpoint
     * @param clientId Application's unique client ID
     * @param openIdProviderUrl URL or domain name of OpenID provider to be used for authentication, in decoded form
     */
    public static AuthorisationRequest openIdAuthorisationRequest(String authUrl, String clientId, String openIdProviderUrl) {
        String requestUrl = new StringBuilder(authUrl).append(QUERY_STRING_MARKER).append(PROVIDER)
                                                      .append(KEY_VALUE_SEPARATOR).append(oAuthHandler.encodeUrl(openIdProviderUrl))
                                                      .toString();
        return new AuthorisationRequest(requestUrl, clientId);
    }

    /**
     *
     * @param url URL of the OAuth 2.0 Authorisation Server's endpoint to associate a new identity with a user
     * @param clientId Application's unique client ID
     * @param openIdProviderUrl URL or domain name of OpenID provider to be used for authentication of the new identity, in decoded form
     * @param accessToken Access token for user authorisation
     * @param newIdentityPrimary True if the new identity should be the user's primary identity,false otherwise
     * @return
     */
    public static AuthorisationRequest associateIdentityRequest(String url, String clientId, String openIdProviderUrl, String accessToken, boolean newIdentityPrimary) {
        String requestUrl = new StringBuilder(url).append(QUERY_STRING_MARKER).append(PROVIDER)
                .append(KEY_VALUE_SEPARATOR).append(oAuthHandler.encodeUrl(openIdProviderUrl))
                .append(PARAMETER_SEPARATOR).append(OAUTH_TOKEN)
                .append(KEY_VALUE_SEPARATOR).append(accessToken)
                .toString();
        if (newIdentityPrimary) {
            requestUrl = new StringBuilder(requestUrl).append(PARAMETER_SEPARATOR).append(NEW_IDENTITY_PRIMARY)
                                                      .append(KEY_VALUE_SEPARATOR).append(TRUE).toString();
        }
        return new AuthorisationRequest(requestUrl, clientId).forceNewRequest(true);
    }

    /**
     *
     * @param url URL of the OAuth 2.0 Authorisation Server's endpoint to set an identity as primary
     * @param openIdIdentityUrl URL of identity to make primary, in decoded form
     * @return
     */
    public static OAuthRequest makeIdentityPrimaryRequest(RequestBuilder.Method method, String url, String openIdIdentityUrl) {
        String requestUrl = new StringBuilder(url).append(QUERY_STRING_MARKER).append(ID)
                .append(KEY_VALUE_SEPARATOR).append(oAuthHandler.encodeUrl(openIdIdentityUrl)).toString();
        return new OAuthRequest(method, requestUrl);
    }
}
