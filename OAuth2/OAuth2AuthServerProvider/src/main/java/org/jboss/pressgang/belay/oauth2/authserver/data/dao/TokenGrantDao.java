package org.jboss.pressgang.belay.oauth2.authserver.data.dao;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.jboss.pressgang.belay.oauth2.authserver.data.model.Scope;
import org.jboss.pressgang.belay.oauth2.authserver.data.model.TokenGrant;
import org.jboss.pressgang.belay.oauth2.authserver.util.AuthServer;
import org.jboss.pressgang.belay.oauth2.shared.data.model.TokenGrantInfo;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.Set;
import java.util.logging.Logger;

import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.ImmutableSet.copyOf;
import static org.apache.amber.oauth2.common.error.OAuthError.CodeResponse.SERVER_ERROR;
import static org.jboss.pressgang.belay.oauth2.shared.data.model.TokenGrantInfo.TokenGrantInfoBuilder.tokenGrantInfoBuilder;

/**
 * TokenGrant DAO.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
@ApplicationScoped
public class TokenGrantDao {

    @Inject
    @AuthServer
    private EntityManager em;

    @Inject
    @AuthServer
    private Logger log;

    @Inject
    private Event<TokenGrant> tokenGrantEventSrc;

    public Optional<TokenGrant> getTokenGrantFromAccessToken(String accessToken) throws OAuthSystemException {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<TokenGrant> criteria = cb.createQuery(TokenGrant.class);
        Root<TokenGrant> tokenGrant = criteria.from(TokenGrant.class);
        criteria.select(tokenGrant).where(cb.equal(tokenGrant.get("accessToken"), accessToken));
        TypedQuery<TokenGrant> query = em.createQuery(criteria);
        if (query.getResultList().size() == 1) {
            log.fine("Returning TokenGrant with access token " + accessToken);
            return Optional.of(query.getSingleResult());
        } else if (query.getResultList().size() > 1) {
            return processQueryWithMultipleResults(query, "access");
        } else {
            log.fine("Could not find TokenGrant with access token " + accessToken);
            return Optional.absent();
        }
    }

    public Optional<TokenGrant> getTokenGrantFromRefreshToken(String refreshToken) throws OAuthSystemException {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<TokenGrant> criteria = cb.createQuery(TokenGrant.class);
        Root<TokenGrant> tokenGrant = criteria.from(TokenGrant.class);
        criteria.select(tokenGrant).where(cb.equal(tokenGrant.get("refreshToken"), refreshToken));
        TypedQuery<TokenGrant> query = em.createQuery(criteria);
        if (query.getResultList().size() == 1) {
            log.fine("Returning TokenGrant with refresh token " + refreshToken);
            return Optional.of(query.getSingleResult());
        } else if (query.getResultList().size() > 1) {
            return processQueryWithMultipleResults(query, "refresh");
        } else {
            log.fine("Could not find TokenGrant with refresh token " + refreshToken);
            return Optional.absent();
        }
    }

    public Optional<TokenGrantInfo> getTokenGrantInfoFromAccessToken(String accessToken) throws OAuthSystemException {
        Optional<TokenGrant> tokenGrantFound = getTokenGrantFromAccessToken(accessToken);
        if (!tokenGrantFound.isPresent()) {
            log.fine("Could not return TokenGrantInfo for access token " + accessToken);
            return Optional.absent();
        } else {
            TokenGrant tokenGrant = tokenGrantFound.get();
            TokenGrantInfo.TokenGrantInfoBuilder builder = tokenGrantInfoBuilder(tokenGrant.getAccessToken())
                    .setAccessTokenExpires(tokenGrant.getAccessTokenExpires())
                    .setAccessTokenExpiry(tokenGrant.getAccessTokenExpiry())
                    .setHasRefreshToken(tokenGrant.getRefreshToken() != null)
                    .setGrantUsername(tokenGrant.getGrantUser().getUsername())
                    .setGrantClientIdentifier(tokenGrant.getGrantClient().getClientIdentifier())
                    .setGrantClientName(tokenGrant.getGrantClient().getClientName())
                    .setGrantClientRedirectUri(tokenGrant.getGrantClient().getClientRedirectUri())
                    .setGrantClientTokensMustExpire(tokenGrant.getGrantClient().getTokenGrantsMustExpire())
                    .setGrantCurrent(tokenGrant.getGrantCurrent())
                    .setGrantTimeStamp(tokenGrant.getGrantTimeStamp());

            if (tokenGrant.getGrantUser().getPrimaryIdentity() != null
                    && tokenGrant.getGrantUser().getPrimaryIdentity().getIdentifier() != null) {
                builder.setGrantUserPrimaryIdentifier(tokenGrant.getGrantUser().getPrimaryIdentity().getIdentifier());
            }

            // Transform grant scopes to a set of scope names
            Set<String> grantScopes = copyOf(transform(tokenGrant.getGrantScopes(),
                    new Function<Scope, String>() {
                        @Override
                        public String apply(Scope scope) {
                            return scope.getScopeName();
                        }
                    }));
            builder.setGrantScopeNames(grantScopes);

            log.fine("Returning TokenGrantInfo for access token " + accessToken);
            return Optional.of(builder.build());
        }
    }

    public void addTokenGrant(TokenGrant tokenGrant) {
        log.info("Adding token grant");
        em.persist(tokenGrant);
        tokenGrantEventSrc.fire(tokenGrant);
    }

    public void updateTokenGrant(TokenGrant tokenGrant) {
        log.info("Updating token grant");
        em.merge(tokenGrant);
        tokenGrantEventSrc.fire(tokenGrant);
    }

    private Optional<TokenGrant> processQueryWithMultipleResults(TypedQuery<TokenGrant> query, String tokenType) throws OAuthSystemException {
        log.fine("Found " + query.getResultList().size() + " " + tokenType + " tokens matching TokenGrant query");
        TokenGrant currentGrant = null;
        int currentGrants = 0;
        for (TokenGrant grant : query.getResultList()) {
            if (grant.getGrantCurrent()) {
                currentGrant = grant;
                currentGrants++;
            }
        }
        if (currentGrants == 1) {
            // Exactly one current grant, which does not violate invariant
            log.fine("Returning sole current TokenGrant from multiple " + tokenType + " token query results");
            return Optional.of(currentGrant);
        } else {
            log.warning("TokenGrant " + tokenType + " token query found multiple results and " + currentGrants
                    + " are current");
            if (currentGrants == 0) {
                log.warning("Not returning TokenGrant as unable to determine single result; multiple non-current grants found");
                return Optional.absent();
            } else {
                log.severe("Multiple(" + currentGrants + ") TokenGrants with same "
                        + tokenType + " token current");
                throw new OAuthSystemException(SERVER_ERROR);
            }
        }
    }
}
