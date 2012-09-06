package org.jboss.pressgang.belay.oauth2.authserver.util;

/**
 * Values common across classes.
 */
public class Constants {

    // Param/attribute standards
    public static final String OPENID_PROVIDER = "provider";
    public static final String AUTHENTICATE_HEADER = "WWW-Authenticate";
    public static final String OPENID_IDENTIFIER = "openid.identifier";
    public static final String OPENID_CLAIMED_ID = "openid.claimed_id";
    public static final String OPENID_AX_PREFIX = "openid.ax.";
    public static final String OPENID_EXT_PREFIX = "openid.ext1.";
    public static final String FIRSTNAME = "firstName";
    public static final String LASTNAME = "lastName";
    public static final String FULLNAME = "fullname";
    public static final String FULLNAME_TITLE_CASE = "fullName";
    public static final String EMAIL = "email";
    public static final String LANGUAGE = "language";
    public static final String COUNTRY = "country";
    public static final String APPLICATION_XRDS_XML = "application/xrds+xml";
    public static final String NEW_IDENTITY_PRIMARY = "newIdentityPrimary";
    public static final String IDENTIFIER = "id";
    public static final String FIRST_IDENTIFIER = "firstIdentifier";
    public static final String QUERY_STRING_MARKER = "?";
    public static final String PARAMETER_SEPARATOR = "&";
    public static final String KEY_VALUE_SEPARATOR = "=";

    // Errors
    public static final String INVALID_IDENTIFIER = "invalid_identifier";
    public static final String INVALID_PROVIDER = "invalid_openid_provider";
    public static final String INVALID_CLIENT_APPLICATION = "Client application is invalid";
    public static final String INVALID_REFRESH_TOKEN = "Refresh token is invalid";
    public static final String INVALID_GRANT_TYPE = "Grant type is unsupported";
    public static final String INVALID_METHOD = "Invalid HTTP method";
    public static final String INVALID_REDIRECT_URI = "invalid_redirect_uri";
    public static final String REALM_ENDPOINT_ERROR = "Could not perform realm endpoint discovery on Relying Party";
    public static final String IDENTITIES_ASSOCIATED_ERROR = "Identities already associated";
    public static final String UNAUTHORISED_QUERY_ERROR = "Unauthorised to query identity";
    public static final String IDENTITY_QUERY_ERROR = "Could not obtain identity information";
    public static final String URL_DECODING_ERROR = "url_decoding_error";
    public static final String INVALID_SESSION = "invalid_session";

    // General
    public static final String ONE_HOUR = "3600";
    public static final String OAUTH_TOKEN_EXPIRY = ONE_HOUR;
    public static final String UTF_ENCODING = "UTF-8";
    public static final String OPENID_REALM = "/OAuth2AuthServer/rest/auth/";
    public static final String LOGIN_ENDPOINT = "/auth/login";
    public static final String COMPLETE_ASSOCIATION_ENDPOINT = "/auth/identity/completeAssociation";
    public static final String OPENID_RETURN_URI = "/OAuth2AuthServer/rest" + LOGIN_ENDPOINT;
    public static final String OAUTH_PROVIDER_ID = "OAuth2AuthServer";
    public static final String STORED_OAUTH_REDIRECT_URI = "storedOAuthRedirectUri";
    public static final String STORED_OAUTH_CLIENT_ID = "storedOAuthClientId";
    public static final String GOOGLE = "google";
    public static final String GMAIL = "gmail";

    // Yadis
    public static final String NS_TAG = "xmlns";
    public static final String XRD_TAG = "XRD";
    public static final String SERVICE_TAG = "Service";
    public static final String TYPE_TAG = "Type";
    public static final String URI_TAG = "URI";
    public static final String XRDS_TAG = "xrds:XRDS";
    public static final String XRDS_NS_TAG = NS_TAG + ":xrds";
    public static final String XRDS_XRI = "xri://$xrds";
    public static final String XRD2_NS_XRI = "xri://$xrd*($v*2.0)";
    public static final String OPENID2_RETURN_TO = "http://specs.openid.net/auth/2.0/return_to";
}