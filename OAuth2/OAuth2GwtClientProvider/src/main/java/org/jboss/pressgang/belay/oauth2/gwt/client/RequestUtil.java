package org.jboss.pressgang.belay.oauth2.gwt.client;

import com.google.gwt.http.client.RequestBuilder;

import static com.google.gwt.http.client.RequestBuilder.Method;
import static org.jboss.pressgang.belay.oauth2.gwt.client.Constants.*;

/**
 * Provides factory methods to create AuthorizationRequests for use with OAuth2 authorization servers using OpenID for
 * authentication, such as the PressGang Belay implementation. Also provides factory methods for requests to PressGang
 * Belay user management and association endpoints.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
public class RequestUtil {

    private static final OAuthHandler oAuthHandler = OAuthHandler.get();

    /**
     * @param authUrl           URL of the OAuth 2.0 Authorization Server's auth endpoint
     * @param clientId          Application's unique client ID
     * @param openIdProviderUrl URL or domain name of OpenID provider to be used for authentication, in decoded form
     */
    public static AuthorizationRequest openIdAuthorizationRequest(String authUrl, String clientId, String openIdProviderUrl) {
        String requestUrl = new StringBuilder(authUrl).append(QUERY_STRING_MARKER).append(PROVIDER)
                .append(KEY_VALUE_SEPARATOR).append(oAuthHandler.encodeUrl(openIdProviderUrl))
                .toString();
        return new AuthorizationRequest(requestUrl, clientId);
    }

    /**
     * Creates an OAuthRequest for an associate identities endpoint. The access token used here should be from the user to
     * associate; the authorizing user should be specified through the authorization used when sending the request.
     *
     * @param method                        HTTP method of the OAuth 2.0 Authorization Server's endpoint to associate user identities
     * @param url                           URL of the OAuth 2.0 Authorization Server's endpoint to associate user identities
     * @param clientId                      Application's unique client ID
     * @param accessTokenForUserToAssociate Access token for the user to associate
     * @param newIdentityPrimary            True if the newly associated user's attributes should be the primary attributes
     *                                      of the resulting user, false otherwise. Defaults to false.
     * @return OAuthRequest with values set
     */
    public static OAuthRequest associateIdentitiesRequest(Method method, String url, String clientId,
                                                          String accessTokenForUserToAssociate, Boolean newIdentityPrimary) {
        OAuthRequest request = new OAuthRequest(method, url);
        StringBuilder requestDataBuilder = new StringBuilder(CLIENT_ID)
                .append(KEY_VALUE_SEPARATOR).append(clientId)
                .append(PARAMETER_SEPARATOR).append(SECOND_TOKEN)
                .append(KEY_VALUE_SEPARATOR).append(accessTokenForUserToAssociate);
        if (newIdentityPrimary != null) {
            requestDataBuilder.append(PARAMETER_SEPARATOR).append(NEW_IDENTITY_PRIMARY)
                    .append(KEY_VALUE_SEPARATOR).append(newIdentityPrimary);
        }
        request.setRequestData(requestDataBuilder.toString());
        request.setHeader(Constants.CONTENT_TYPE, URL_ENCODED_TYPE);
        return request;
    }

    /**
     * @param method            HTTP method of the OAuth 2.0 Authorization Server's endpoint to make an identity primary
     * @param url               URL of the OAuth 2.0 Authorization Server's endpoint to set an identity as primary
     * @param openIdIdentityUrl URL of identity to make primary, in decoded form
     * @return
     */
    public static OAuthRequest makeIdentityPrimaryRequest(Method method, String url, String openIdIdentityUrl) {
        String requestUrl = new StringBuilder(url).append(QUERY_STRING_MARKER).append(ID)
                .append(KEY_VALUE_SEPARATOR).append(oAuthHandler.encodeUrl(openIdIdentityUrl)).toString();
        return new OAuthRequest(method, requestUrl);
    }

    /**
     * @param method   HTTP method of the OAuth 2.0 Authorization Server's endpoint to invalidate a token grant
     * @param url      URL of the OAuth 2.0 Authorization Server's endpoint to invalidate a token grant
     * @param clientId Application's unique client ID
     * @return
     */
    public static OAuthRequest invalidateTokenGrantRequest(Method method, String url, String clientId) {
        String requestUrl = new StringBuilder(url).append(QUERY_STRING_MARKER).append(CLIENT_ID)
                .append(KEY_VALUE_SEPARATOR).append(clientId).toString();
        return new OAuthRequest(method, requestUrl);
    }
}
