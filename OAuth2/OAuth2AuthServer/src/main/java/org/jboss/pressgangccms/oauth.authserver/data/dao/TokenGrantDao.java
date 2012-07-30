package org.jboss.pressgangccms.oauth.authserver.data.dao;

import com.google.appengine.repackaged.com.google.common.base.Optional;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.jboss.pressgangccms.oauth.authserver.data.model.TokenGrant;
import org.jboss.pressgangccms.oauth.authserver.data.model.TokenGrant_;
import org.jboss.pressgangccms.oauth.authserver.util.Common;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.logging.Logger;

/**
 * TokenGrant DAO.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
@ApplicationScoped
public class TokenGrantDao {

    @Inject
    private EntityManager em;

    @Inject
    private Logger log;

    public Optional<TokenGrant> getTokenGrantFromAccessToken(String accessToken) throws OAuthSystemException {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<TokenGrant> criteria = cb.createQuery(TokenGrant.class);
        Root<TokenGrant> tokenGrant = criteria.from(TokenGrant.class);
        criteria.select(tokenGrant).where(cb.equal(tokenGrant.get(TokenGrant_.accessToken), accessToken));
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
        criteria.select(tokenGrant).where(cb.equal(tokenGrant.get(TokenGrant_.refreshToken), refreshToken));
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
                throw new OAuthSystemException(Common.SYSTEM_ERROR);
            }
        }
    }
}
