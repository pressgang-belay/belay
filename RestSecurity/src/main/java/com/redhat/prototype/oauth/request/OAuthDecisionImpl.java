package com.redhat.prototype.oauth.request;

import com.redhat.prototype.Common;
import com.redhat.prototype.model.auth.Scope;
import com.redhat.prototype.model.auth.TokenGrant;
import com.redhat.prototype.service.AuthService;
import org.apache.amber.oauth2.rsfilter.OAuthClient;
import org.apache.amber.oauth2.rsfilter.OAuthDecision;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.Set;

public class OAuthDecisionImpl implements OAuthDecision {

    private AuthService authService;
    private String realm;
    private TokenGrant tokenGrant;
    private OAuthClient oAuthClient;
    private Principal principal;
    private boolean isAuthorized;

    public OAuthDecisionImpl(String realm, String token, HttpServletRequest request) {
        this.isAuthorized = false;
        this.realm = realm;
        if (token.toLowerCase().startsWith(Common.BEARER)) {
            token = token.substring(Common.BEARER.length()).trim();
        }
        try {
            this.authService = (AuthService) new InitialContext().lookup("java:global/RestSecurity/AuthService");
        } catch (NamingException e) {
            e.printStackTrace();
            return;
        }
        this.tokenGrant = authService.getTokenGrant(token);
        if (tokenGrant != null) {
            this.oAuthClient = new OAuthClientImpl(tokenGrant.getGrantClient());
            this.principal = new UserPrincipal(tokenGrant.getGrantUser());
            setAuthorisation(tokenGrant, request);
        } else {
            this.isAuthorized = false;
            this.oAuthClient = getDefaultClient();
            this.principal = getDefaultPrincipal(request);
        }
    }

    private void setAuthorisation(TokenGrant tokenGrant, HttpServletRequest request) {
        Set<Scope> tokenGrantScopes = tokenGrant.getGrantScopes();
        //TODO check scopes
        isAuthorized = true;
    }

    @Override
    public boolean isAuthorized() {
        return isAuthorized;
    }

    @Override
    public Principal getPrincipal() {
        return principal;
    }

    @Override
    public OAuthClient getOAuthClient() {
        return oAuthClient;
    }

    private OAuthClient getDefaultClient() {
        return new OAuthClient() {
            @Override
            public String getClientId() {
                return null;
            }
        };
    }

    private Principal getDefaultPrincipal(HttpServletRequest request) {
        return request.getUserPrincipal();
    }
}
