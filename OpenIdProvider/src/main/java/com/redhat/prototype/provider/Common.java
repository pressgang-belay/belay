package com.redhat.prototype.provider;

public class Common {

    //  Request parameters
    public static final String ACTION = "_action";

    // Parameter values
    public static final String COMPLETE = "complete";

    // Session attributes
    public static final String PARAM_LIST = "parameterlist";
    public static final String AUTHENTICATED_APPROVED = "authenticatedAndApproved";
    public static final String OPENID_IDENTITY = "openid.identity";
    public static final String OPENID_CLAIMED = "openid.claimed_id";
    public static final String OPENID_MODE = "openid.mode";

    // Modes
    public static final String ASSOCIATE = "associate";
    public static final String CHECK_AUTHENTICATION = "check_authentication";
    public static final String CHECKID_SETUP = "checkid_setup";
    public static final String CHECKID_IMMEDIATE = "checkid_immediate";

    // Errors
    public static final String NO_OPENID_PARAMS_ERROR = "No OpenId parameters found";
    public static final String UNKNOWN_REQUEST_ERROR = "Unknown request";
    public static final String USER_ENDPOINT_ERROR = "Could not perform discovery on user identifier";

    // Yadis
    public static final String NS_TAG = "xmlns";
    public static final String XRD_TAG = "XRD";
    public static final String SERVICE_TAG = "Service";
    public static final String URI_TAG = "URI";
    public static final String TYPE_TAG = "Type";
    public static final String XRDS_TAG = "xrds:XRDS";
    public static final String XRDS_NS_TAG = NS_TAG + ":xrds";
    public static final String XRDS_XRI = "xri://$xrds";
    public static final String XRD2_NS_XRI = "xri://$xrd*($v*2.0)";
    public static final String PRIORITY = "priority";
    public static final String FIRST = "0";
    public static final String OPENID2_SIGNON = "http://specs.openid.net/auth/2.0/signon";
    public static final String OPENID_AX_EXT = "http://openid.net/srv/ax/1.0";

    // General
    public static final String APPLICATION_XRDS_XML = "application/xrds+xml";
    public static final String PRE_TAG_OPEN = "<pre>";
    public static final String PRE_TAG_CLOSE = "</pre>";
    public static final String SCHEME_END = "://";
    public static final String COLON = ":";

    public static final String PROVIDER_ENDPOINT = "/openid/provider";
}
