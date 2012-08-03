package org.jboss.pressgangccms.oauth2.authserver.data.dao;

import com.google.common.base.Optional;
import org.jboss.pressgangccms.oauth2.authserver.data.model.Scope;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.logging.Logger;

/**
 * Scope DAO.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
@ApplicationScoped
public class ScopeDao {

    @Inject
    private EntityManager em;

    @Inject
    private Logger log;

    private static final String DEFAULT_SCOPE_NAME = "default";

    public Scope getDefaultScope() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Scope> criteria = cb.createQuery(Scope.class);
        Root<Scope> scope = criteria.from(Scope.class);
        criteria.select(scope).where(cb.equal(scope.get("scopeName"), DEFAULT_SCOPE_NAME));
        log.fine("Returning default Scope");
        return em.createQuery(criteria).getSingleResult();
    }

    public Optional<Scope> getScopeFromName(String name) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Scope> criteria = cb.createQuery(Scope.class);
        Root<Scope> scope = criteria.from(Scope.class);
        criteria.select(scope).where(cb.equal(scope.get("scopeName"), name));
        TypedQuery<Scope> query = em.createQuery(criteria);
        if (query.getResultList().size() == 1) {
            log.fine("Returning Scope with name " + name);
            return Optional.of(query.getSingleResult());
        } else {
            log.fine("Could not find Scope with name " + name);
            return Optional.absent();
        }
    }
}
