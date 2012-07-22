package org.jboss.pressgangccms.oauth.server.rest.auth;

import com.google.appengine.repackaged.com.google.common.base.Optional;
import org.apache.amber.oauth2.as.response.OAuthASResponse;
import org.apache.amber.oauth2.common.exception.OAuthProblemException;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.apache.http.HttpRequest;
import org.jboss.pressgangccms.oauth.server.data.model.auth.ClientApplication;
import org.jboss.pressgangccms.oauth.server.data.model.auth.Scope;
import org.jboss.pressgangccms.oauth.server.data.model.auth.TokenGrant;
import org.jboss.pressgangccms.oauth.server.data.model.auth.User;
import org.jboss.pressgangccms.oauth.server.service.AuthService;
import org.jboss.pressgangccms.oauth.server.service.TokenIssuerService;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Set;
import java.util.logging.Logger;

import static com.google.appengine.repackaged.com.google.common.collect.Sets.newHashSet;
import static org.jboss.pressgangccms.oauth.server.util.Common.INVALID_SCOPE;
import static org.jboss.pressgangccms.oauth.server.util.Common.ONE_HOUR;
import static org.jboss.pressgangccms.oauth.server.util.Common.OPENID_IDENTIFIER;

/**
 * Encapsulates logic shared across auth web services.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
public class OAuthUtil {

    static TokenGrant createTokenGrantWithDefaults(TokenIssuerService tokenIssuerService, AuthService authService,
                                                   User user, ClientApplication client) throws OAuthSystemException {
        TokenGrant tokenGrant = new TokenGrant();
        tokenGrant.setGrantUser(user);
        tokenGrant.setGrantClient(client);
        tokenGrant.setGrantScopes(newHashSet(authService.getDefaultScope()));
        tokenGrant.setAccessToken(tokenIssuerService.accessToken());
        tokenGrant.setRefreshToken(tokenIssuerService.refreshToken());
        tokenGrant.setAccessTokenExpiry(ONE_HOUR);
        tokenGrant.setGrantTimeStamp(new Date());
        tokenGrant.setGrantCurrent(true);
        return tokenGrant;
    }

    /**
     * Checks user scopes contains all requested scopes. Throws OAuthProblemException if invalid scope requested.
     *
     * @param authService     AuthService to use
     * @param requestedScopes Set of scopes requested for token grant
     * @param userScopes      Set of user's scopes
     * @return
     * @throws OAuthProblemException if scope requested not found or user does not have that scope
     */
    static Set<Scope> checkScopes(AuthService authService, Set<String> requestedScopes, Set<Scope> userScopes)
            throws OAuthProblemException {
        Set<Scope> grantScopes = newHashSet(authService.getDefaultScope());
        for (String scopeName : requestedScopes) {
            Optional<Scope> scopeFound = authService.getScopeByName(scopeName);
            if ((!scopeFound.isPresent()) || (!userScopes.contains(scopeFound.get()))) {
                throw OAuthProblemException.error(INVALID_SCOPE);
            } else {
                grantScopes.add(scopeFound.get());
            }
        }
        return grantScopes;
    }

    static void makeGrantsNonCurrent(AuthService authService, Set<TokenGrant> grants) {
        for (TokenGrant grant : grants) {
            if (grant.getGrantCurrent()) {
                grant.setGrantCurrent(false);
                authService.updateGrant(grant);
            }
        }
    }

    static OAuthASResponse.OAuthTokenResponseBuilder addTokenGrantResponseParams(TokenGrant tokenGrant,
            int statusCode) {
        OAuthASResponse.OAuthTokenResponseBuilder builder = OAuthASResponse
                .tokenResponse(statusCode);
        builder.setAccessToken(tokenGrant.getAccessToken());
        builder.setRefreshToken(tokenGrant.getRefreshToken());
        builder.setExpiresIn(tokenGrant.getAccessTokenExpiry());
        return builder;
    }

    static String getStringAttributeFromSessionAndRemove(HttpServletRequest request, Logger log, String attributeKey,
                                                         String attributeName) {
        String attribute = (String) request.getSession().getAttribute(attributeKey);
        log.info(attributeName + " is: " + attribute);
        request.getSession().removeAttribute(attributeKey);
        return attribute;
    }

    //@SuppressWarnings("unchecked")
    static Set<String> getStringSetAttributeFromSessionAndRemove(HttpServletRequest request, Logger log,
                                                                 String attributeKey, String attributeName) {
        Set<String> attribute = (Set<String>) request.getSession().getAttribute(attributeKey);
        log.info(attributeName + " is: " + attribute);
        request.getSession().removeAttribute(attributeKey);
        return attribute;
    }

    static Boolean getBooleanAttributeFromSessionAndRemove(HttpServletRequest request, Logger log,
                                                                 String attributeKey, String attributeName) {
        Boolean attribute = (Boolean) request.getSession().getAttribute(attributeKey);
        log.info(attributeName + " is: " + attribute);
        request.getSession().removeAttribute(attributeKey);
        return attribute;
    }
}
