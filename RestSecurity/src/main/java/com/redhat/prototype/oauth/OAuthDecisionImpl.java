package com.redhat.prototype.oauth;

import org.apache.amber.oauth2.common.OAuth;
import org.apache.amber.oauth2.rsfilter.OAuthClient;
import org.apache.amber.oauth2.rsfilter.OAuthDecision;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;

public class OAuthDecisionImpl implements OAuthDecision {

    private OAuthClient oAuthClient;
    private Principal principal;
    private String token;
    private String realm;
    private HttpServletRequest request;

    //TODO
    public OAuthDecisionImpl(String realm, String token, HttpServletRequest request) {
        this.oAuthClient = new OAuthClientImpl(request.getHeader(OAuth.OAUTH_CLIENT_ID));
        this.principal = new PrincipalImpl("" , request.getUserPrincipal());
        this.token = token;
        this.realm = realm;
        this.request = request;
    }

    @Override
    public boolean isAuthorized() {
        // check token and req
        return true; //TODO
    }

    @Override
    public Principal getPrincipal() {
        return principal;
    }

    @Override
    public OAuthClient getOAuthClient() {
        return oAuthClient; //TODO
    }
}
