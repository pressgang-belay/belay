package com.redhat.prototype.util;

public class Common {

    // Param/attribute standards
    public static final String BEARER = "bearer";
    public static final String OPENID_PROVIDER = "provider";
    public static final String AUTHENTICATE_HEADER = "WWW-Authenticate";
    public static final String OPENID_IDENTIFIER = "openid.identifier";
    public static final String OPENID_FIRSTNAME = "openid.ax.firstname";
    public static final String OPENID_LASTNAME = "openid.ax.lastname";
    public static final String OPENID_EMAIL = "openid.ax.email";
    public static final String OPENID_LANGUAGE = "openid.ax.language";
    public static final String OPENID_COUNTRY = "openid.ax.country";
    public static final String APPLICATION_XRDS_XML = "application/xrds+xml";

    // Errors
    public static final String OAUTH_CALLBACK_URL_REQUIRED = "OAuth callback URL needs to be provided by client";
    public static final String SYSTEM_ERROR = "System error";
    public static final String INVALID_USER_IDENTIFIER = "Invalid user identifier";
    public static final String INVALID_CALLBACK_URI = "Invalid callback URI";
    public static final String INVALID_SCOPE = "Invalid scope requested";
    public static final String INVALID_PROVIDER = "Invalid OpenId provider";
    public static final String INVALID_CLIENT = "Invalid client application";
    public static final String INVALID_REFRESH_TOKEN = "Invalid refresh token";
    public static final String INVALID_GRANT_TYPE = "Cannot process request with this grant_type";
    public static final String INVALID_METHOD = "Method not set to GET";
    public static final String INVALID_RESPONSE_TYPE = "Unsupported or invalid response_type parameter value";
    public static final String MISSING_RESPONSE_TYPE = "Missing response_type parameter value";
    public static final String REALM_ENDPOINT_ERROR = "Could not perform realm endpoint discovery on Relying Party";

    // General
    public static final String ONE_HOUR = "3600";
    public static final String UTF_ENCODING = "UTF-8";
    public static final String OPENID_REALM = "https://localhost:8443/RestSecurity/rest/auth/";
    public static final String OPENID_RETURN_URL = "https://localhost:8443/RestSecurity/rest/auth/login";

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
