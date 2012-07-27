package org.jboss.pressgangccms.oauth.server.util;

/**
 * Values common across classes.
 */
public class Common {

    // Param/attribute standards
    public static final String BEARER = "bearer";
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
    public static final String OAUTH_CALLBACK_URL_REQUIRED = "OAuth callback URL needs to be provided by client";
    public static final String SYSTEM_ERROR = "System error";
    public static final String INVALID_IDENTIFIER = "Invalid identifier";
    public static final String INVALID_CALLBACK_URI = "Invalid callback URI";
    public static final String INVALID_SCOPE = "Invalid scope requested";
    public static final String INVALID_PROVIDER = "Invalid OpenId provider";
    public static final String INVALID_CLIENT_APPLICATION = "Invalid client application";
    public static final String INVALID_REFRESH_TOKEN = "Invalid refresh token";
    public static final String INVALID_GRANT_TYPE = "Cannot process request with this grant_type";
    public static final String INVALID_METHOD = "Method not set to GET";
    public static final String INVALID_RESPONSE_TYPE = "Unsupported or invalid response_type parameter value";
    public static final String MISSING_RESPONSE_TYPE = "Missing response_type parameter value";
    public static final String REALM_ENDPOINT_ERROR = "Could not perform realm endpoint discovery on Relying Party";
    public static final String IDENTITIES_ASSOCIATED_ERROR = "Identities already associated";
    public static final String UNAUTHORISED_QUERY_ERROR = "Unauthorised to query identity";
    public static final String IDENTITY_QUERY_ERROR = "Could not obtain identity information";
    public static final String URL_DECODING_ERROR = "URL decoding error";

    // General
    public static final String ONE_HOUR = "3600";
    public static final String UTF_ENCODING = "UTF-8";
    public static final String OPENID_REALM = "https://localhost:8443/OAuthProvider/rest/auth/";
    public static final String LOGIN_ENDPOINT = "/auth/login";
    public static final String COMPLETE_ASSOCIATION_ENDPOINT = "/auth/identity/completeAssociation";
    public static final String OPENID_RETURN_URL = "https://localhost:8443/OAuthProvider/rest" + LOGIN_ENDPOINT;
    public static final String OAUTH_PROVIDER_ID = "OAuthProvider";
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
