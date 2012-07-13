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
    public static final String OPENID_COUNTRY = "openid.ax.country";

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


    // General
    public static final String ONE_HOUR = "3600";
    public static final String UTF_ENCODING = "UTF-8";
}
