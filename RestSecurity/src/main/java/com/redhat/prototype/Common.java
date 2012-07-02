package com.redhat.prototype;

import org.apache.amber.oauth2.client.request.OAuthClientRequest;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class Common {

    public static final String RESOURCE_SERVER_NAME = "Example OAuth Resource Server";
    public static final String ACCESS_TOKEN_VALID = "access_token_valid";
    public static final String ACCESS_TOKEN_EXPIRED = "access_token_expired";
    public static final String ACCESS_TOKEN_INSUFFICIENT = "access_token_insufficient";
    public static final String WWW_AUTHENTICATE = "WWW-Authenticate";

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

    public static final String HEADER_WWW_AUTHENTICATE = "WWW-Authenticate";
    public static final String HEADER_AUTHORIZATION = "Authorization";

    public static final String AUTHORIZATION_CODE = "known_authz_code";

    public static final String ASSERTION = "<samlp:AuthnRequest\n"
            + "   xmlns:samlp=\"urn:oasis:names:tc:SAML:2.0:protocol\"\n"
            + "   xmlns:saml=\"urn:oasis:names:tc:SAML:2.0:assertion\"\n"
            + "   ID=\"aaf23196-1773-2113-474a-fe114412ab72\"\n"
            + "   Version=\"2.0\"\n"
            + "   IssueInstant=\"2004-12-05T09:21:59Z\"\n"
            + "   AssertionConsumerServiceIndex=\"0\"\n"
            + "   AttributeConsumingServiceIndex=\"0\">\n"
            + "   <saml:Issuer>https://sp.example.com/SAML2</saml:Issuer>\n"
            + "   <samlp:NameIDPolicy\n"
            + "     AllowCreate=\"true\"\n"
            + "     Format=\"urn:oasis:names:tc:SAML:2.0:nameid-format:transient\"/>\n"
            + " </samlp:AuthnRequest>";
    public static final String ASSERTION_TYPE = "http://xml.coverpages.org/saml.html";

    public static final String ACCESS_TOKEN_ENDPOINT = "http://localhost:9001/oauth/oauth2/token";
    public static final String AUTHORIZATION_ENPOINT = "http://localhost:9001/oauth/oauth2/authz";
    public static final String REDIRECT_URL = "http://localhost:9002/oauth/oauth2/redirect";
    public static final String RESOURCE_SERVER = "http://localhost:9003/resource_server";
    public static final String PROTECTED_RESOURCE_HEADER = "/resource_header";
    public static final String PROTECTED_RESOURCE_BODY = "/resource_body";
    public static final String PROTECTED_RESOURCE_QUERY = "/resource_query";

    public static final String TEST_WEBAPP_PATH = "/server";
    public static final String OPENID_PROVIDER = "provider";

    public static HttpURLConnection doRequest(OAuthClientRequest req) throws IOException {
        URL url = new URL(req.getLocationUri());
        HttpURLConnection c = (HttpURLConnection)url.openConnection();
        c.setInstanceFollowRedirects(true);
        c.connect();
        c.getResponseCode();

        return c;
    }

    // CommonExt code

    public static final String REGISTRATION_ENDPOINT = "http://localhost:9000/auth/oauth2ext/register";
    public static final String APP_NAME = "Sample Application";
    public static final String APP_URL = "http://www.example.com";
    public static final String APP_ICON = "http://www.example.com/app.ico";
    public static final String APP_DESCRIPTION = "Description of a Sample App";
    public static final String APP_REDIRECT_URI = "http://www.example.com/redirect";

    public static final String CLIENT_ID_EXT = "someclientid";
    public static final String CLIENT_SECRET_EXT = "someclientsecret";
    public static final String ISSUED_AT = "0123456789";
    public static final String EXPIRES_IN = "3600";

    public static final String ONE_HOUR = "3600";
}
