package org.jboss.pressgang.belay.oauth2.authserver.data.dao;

import com.google.common.base.Optional;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.jboss.pressgang.belay.oauth2.authserver.data.model.CodeGrant;
import org.jboss.pressgang.belay.oauth2.authserver.util.AuthServer;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.logging.Logger;

import static org.apache.amber.oauth2.common.error.OAuthError.CodeResponse.SERVER_ERROR;

/**
 * CodeGrant DAO.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
@ApplicationScoped
public class CodeGrantDao {

    @Inject
    @AuthServer
    private EntityManager em;

    @Inject
    @AuthServer
    private Logger log;

    @Inject
    private Event<CodeGrant> codeGrantEventSrc;

    public Optional<CodeGrant> getCodeGrantFromAuthCode(String authCode) throws OAuthSystemException {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<CodeGrant> criteria = cb.createQuery(CodeGrant.class);
        Root<CodeGrant> codeGrant = criteria.from(CodeGrant.class);
        criteria.select(codeGrant).where(cb.equal(codeGrant.get("authCode"), authCode));
        TypedQuery<CodeGrant> query = em.createQuery(criteria);
        if (query.getResultList().size() == 1) {
            log.fine("Returning CodeGrant with auth code " + authCode);
            return Optional.of(query.getSingleResult());
        } else if (query.getResultList().size() > 1) {
            return processQueryWithMultipleResults(query);
        } else {
            log.fine("Could not find CodeGrant with auth code " + authCode);
            return Optional.absent();
        }
    }

    public void addCodeGrant(CodeGrant codeGrant) {
        log.info("Adding code grant");
        em.persist(codeGrant);
        codeGrantEventSrc.fire(codeGrant);
    }

    public void updateCodeGrant(CodeGrant codeGrant) {
        log.info("Updating token grant");
        em.merge(codeGrant);
        codeGrantEventSrc.fire(codeGrant);
    }

    private Optional<CodeGrant> processQueryWithMultipleResults(TypedQuery<CodeGrant> query) throws OAuthSystemException {
        log.fine("Found " + query.getResultList().size() + " auth codes matching CodeGrant query");
        CodeGrant currentGrant = null;
        int currentGrants = 0;
        for (CodeGrant grant : query.getResultList()) {
            if (grant.getGrantCurrent()) {
                currentGrant = grant;
                currentGrants++;
            }
        }
        if (currentGrants == 1) {
            // Exactly one current grant, which does not violate invariant
            log.fine("Returning sole current CodeGrant from multiple auth code query results");
            return Optional.of(currentGrant);
        } else {
            log.warning("CodeGrant auth code query found multiple results and " + currentGrants
                    + " are current");
            if (currentGrants == 0) {
                log.warning("Not returning CodeGrant as unable to determine single result; multiple non-current grants found");
                return Optional.absent();
            } else {
                log.severe("Multiple(" + currentGrants + ") CodeGrants with same auth code are current");
                throw new OAuthSystemException(SERVER_ERROR);
            }
        }
    }
}
