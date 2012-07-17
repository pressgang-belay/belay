package org.jboss.pressgangccms.openid.provider;

/**
 * Values common across classes.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
class Common {

    //  Request parameters
    static final String ACTION = "_action";
    static final String ID = "id";

    // Parameter values
    static final String COMPLETE = "complete";

    // Session attributes
    static final String PARAM_LIST = "parameterlist";
    static final String AUTHENTICATED_APPROVED = "authenticatedAndApproved";
    static final String OPENID_IDENTITY = "openid.identity";
    static final String OPENID_CLAIMED = "openid.claimed_id";
    static final String OPENID_MODE = "openid.mode";

    // Modes
    static final String ASSOCIATE = "associate";
    static final String CHECK_AUTHENTICATION = "check_authentication";
    static final String CHECKID_SETUP = "checkid_setup";
    static final String CHECKID_IMMEDIATE = "checkid_immediate";

    // Errors
    static final String NO_OPENID_PARAMS_ERROR = "No OpenId parameters found";
    static final String UNKNOWN_REQUEST_ERROR = "Unknown request";
    static final String USER_ENDPOINT_ERROR = "Could not perform discovery on user identifier";

    // Yadis
    static final String NS_TAG = "xmlns";
    static final String XRD_TAG = "XRD";
    static final String SERVICE_TAG = "Service";
    static final String URI_TAG = "URI";
    static final String TYPE_TAG = "Type";
    static final String XRDS_TAG = "xrds:XRDS";
    static final String XRDS_NS_TAG = NS_TAG + ":xrds";
    static final String XRDS_XRI = "xri://$xrds";
    static final String XRD2_NS_XRI = "xri://$xrd*($v*2.0)";
    static final String PRIORITY = "priority";
    static final String FIRST = "0";
    static final String OPENID2_SIGNON = "http://specs.openid.net/auth/2.0/signon";
    static final String OPENID_AX_EXT = "http://openid.net/srv/ax/1.0";

    // General
    static final String APPLICATION_XRDS_XML = "application/xrds+xml";
    static final String TEXT_PLAIN = "text/plain";
    static final String PRE_TAG_OPEN = "<pre>";
    static final String PRE_TAG_CLOSE = "</pre>";
    static final String QUERY_STRING_MARKER = "?";
    static final String KEY_VALUE_SEPARATOR = "=";

    static final String PROVIDER_ENDPOINT = "https://localhost:8443/OpenIdProvider/openid/provider";
}
