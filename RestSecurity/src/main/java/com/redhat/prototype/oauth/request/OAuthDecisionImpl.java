package com.redhat.prototype.oauth.request;

import com.google.appengine.repackaged.org.joda.time.DateTime;
import com.redhat.prototype.Common;
import com.redhat.prototype.model.auth.TokenGrant;
import com.redhat.prototype.service.AuthService;
import org.apache.amber.oauth2.common.error.OAuthError;
import org.apache.amber.oauth2.common.exception.OAuthProblemException;
import org.apache.amber.oauth2.rsfilter.OAuthClient;
import org.apache.amber.oauth2.rsfilter.OAuthDecision;
import org.joda.time.LocalDate;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.Date;

import static com.redhat.prototype.Common.SYSTEM_ERROR;

public class OAuthDecisionImpl implements OAuthDecision {

    private AuthService authService;
    private String realm;
    private TokenGrant tokenGrant;
    private OAuthClient oAuthClient;
    private Principal principal;
    private boolean isAuthorized;
    private String authServiceJndiAddress = "java:global/RestSecurity/AuthService";

    public OAuthDecisionImpl(String realm, String token, HttpServletRequest request) throws OAuthProblemException {
        this.realm = realm;
        if (token.toLowerCase().startsWith(Common.BEARER)) {
            // Remove leading header
            token = token.substring(Common.BEARER.length()).trim();
        }
        try {
            this.authService = (AuthService) new InitialContext().lookup(authServiceJndiAddress);
        } catch (NamingException e) {
            e.printStackTrace();
            throw OAuthProblemException.error(SYSTEM_ERROR);
        }
        System.out.println("Evaluating parsed token: " + token);
        this.tokenGrant = authService.getTokenGrantByAccessToken(token);
        if (tokenGrant != null) {
            this.oAuthClient = new OAuthClientImpl(tokenGrant.getGrantClient());
            this.principal = new UserPrincipal(tokenGrant.getGrantUser());
            setAuthorisation(tokenGrant, request);
        } else {
            this.isAuthorized = false;
            this.oAuthClient = getDefaultClient();
            this.principal = getDefaultPrincipal(request);
            throw OAuthProblemException.error(OAuthError.ResourceResponse.INVALID_TOKEN);
        }
    }

    private void setAuthorisation(TokenGrant tokenGrant, HttpServletRequest request) throws OAuthProblemException {
        //Set<Scope> tokenGrantScopes = tokenGrant.getGrantScopes();
        //TODO check scopes
        // Throw exception if token has expired
        DateTime expiryDate = new DateTime(tokenGrant.getGrantTimeStamp()).
                plusSeconds(Integer.parseInt(tokenGrant.getAccessTokenExpiry()));
        if (expiryDate.isBeforeNow()) {
            isAuthorized = false;
            throw OAuthProblemException.error(OAuthError.ResourceResponse.INVALID_TOKEN);
        }
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
