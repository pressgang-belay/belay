package org.jboss.pressgang.belay.oauth2.authserver.data.dao;

import com.google.common.base.Optional;
import org.jboss.pressgang.belay.oauth2.authserver.data.model.Scope;
import org.jboss.pressgang.belay.oauth2.authserver.util.AuthServer;
import org.jboss.pressgang.belay.oauth2.authserver.util.Resources;

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

/**
 * Scope DAO.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
@ApplicationScoped
public class ScopeDao {

    @Inject
    @AuthServer
    private EntityManager em;

    @Inject
    @AuthServer
    private Logger log;

    @Inject
    private Event<Scope> scopeEventSrc;

    public Scope getDefaultScope() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Scope> criteria = cb.createQuery(Scope.class);
        Root<Scope> scope = criteria.from(Scope.class);
        criteria.select(scope).where(cb.equal(scope.get("scopeName"), Resources.defaultScopeName));
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
            Scope result = query.getSingleResult();
            log.fine("Returning Scope with name " + name);
            return Optional.of(result);
        } else {
            log.fine("Could not find Scope with name " + name);
            return Optional.absent();
        }
    }
}
