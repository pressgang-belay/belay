package com.redhat.prototype;

public class Common {

    public static final String ACCESS_TOKEN_VALID = "access_token_valid";
    public static final String ACCESS_TOKEN_EXPIRED = "access_token_expired";
    public static final String ACCESS_TOKEN_INSUFFICIENT = "access_token_insufficient";

    public static final String OAUTH_VERSION_1
            = "oauth_token=\"some_oauth1_token\",realm=\"Something\",oauth_signature_method=\"HMAC-SHA1\"";
    public static final String OAUTH_VERSION_2 = ACCESS_TOKEN_VALID;
    public static final String OAUTH_VERSION_2_EXPIRED = ACCESS_TOKEN_EXPIRED;
    public static final String OAUTH_VERSION_2_INSUFFICIENT = ACCESS_TOKEN_INSUFFICIENT;

    public static final String OAUTH_URL_ENCODED_VERSION_1 = OAUTH_VERSION_1;
    public static final String OAUTH_URL_ENCODED_VERSION_2 = "access_token=" + OAUTH_VERSION_2;
    public static final String OAUTH_URL_ENCODED_VERSION_2_EXPIRED = "access_token=" + OAUTH_VERSION_2_EXPIRED;
    public static final String OAUTH_URL_ENCODED_VERSION_2_INSUFFICIENT = "access_token="
            + OAUTH_VERSION_2_INSUFFICIENT;

    public static final String AUTHORIZATION_HEADER_OAUTH1 = "OAuth " + OAUTH_VERSION_1;
    public static final String AUTHORIZATION_HEADER_OAUTH2 = "Bearer " + OAUTH_VERSION_2;
    public static final String AUTHORIZATION_HEADER_OAUTH2_EXPIRED = "Bearer " + OAUTH_VERSION_2_EXPIRED;
    public static final String AUTHORIZATION_HEADER_OAUTH2_INSUFFICIENT = "Bearer "
            + OAUTH_VERSION_2_INSUFFICIENT;

    public static final String BODY_OAUTH1 = OAUTH_URL_ENCODED_VERSION_1;
    public static final String BODY_OAUTH2 = OAUTH_URL_ENCODED_VERSION_2;
    public static final String BODY_OAUTH2_EXPIRED = OAUTH_URL_ENCODED_VERSION_2_EXPIRED;
    public static final String BODY_OAUTH2_INSUFFICIENT = OAUTH_URL_ENCODED_VERSION_2_INSUFFICIENT;

    public static final String QUERY_OAUTH1 = OAUTH_URL_ENCODED_VERSION_1;
    public static final String QUERY_OAUTH2 = OAUTH_URL_ENCODED_VERSION_2;
    public static final String QUERY_OAUTH2_EXPIRED = OAUTH_URL_ENCODED_VERSION_2_EXPIRED;
    public static final String QUERY_OAUTH2_INSUFFICIENT = OAUTH_URL_ENCODED_VERSION_2_INSUFFICIENT;

    public static final String CLIENT_ID = "skynet_id";
    public static final String CLIENT_SECRET = "test_secret";
    public static final String USERNAME = "user";
    public static final String PASSWORD = "password";

    public static final String BEARER = "bearer";

    public static final String OPENID_PROVIDER = "provider";
    public static final String ONE_HOUR = "3600";
}
