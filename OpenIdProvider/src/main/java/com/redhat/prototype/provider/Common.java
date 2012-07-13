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
    public static final String MISSING_IDENTITY_ERROR = "OpenId identity was not supplied";
    public static final String UNKNOWN_REQUEST_ERROR = "Unknown request";

    // General
    public static final String PRE_TAG_OPEN = "<pre>";
    public static final String PRE_TAG_CLOSE = "</pre>";
    public static final String SCHEME_END = "://";
    public static final String COLON = ":";

    public static final String PROVIDER_ENDPOINT = "/openid/provider/";
}
