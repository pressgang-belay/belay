package org.jboss.pressgangccms.oauth2.resourceserver.data.dao;

import org.jboss.pressgangccms.oauth2.resourceserver.data.model.OAuth2RSEndpoint;
import org.jboss.pressgangccms.oauth2.resourceserver.data.model.OAuth2RSScope;
import org.jboss.pressgangccms.oauth2.resourceserver.util.ResourceServer;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.Set;
import java.util.logging.Logger;

import static com.google.common.collect.Sets.newHashSet;

/**
 * Resource server scope DAO.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
@RequestScoped
public class OAuth2RSScopeDao {

    @Inject
    @ResourceServer
    private Logger log;

    @Inject
    @ResourceServer
    private EntityManager em;

    public Set<OAuth2RSEndpoint> findEndpointsForScopeName(String scopeName) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<OAuth2RSScope> criteria = cb.createQuery(OAuth2RSScope.class);
        Root<OAuth2RSScope> scope = criteria.from(OAuth2RSScope.class);
        criteria.select(scope).where(cb.equal(scope.get("scopeName"), scopeName));
        TypedQuery<OAuth2RSScope> query = em.createQuery(criteria);
        if (query.getResultList().size() != 1) {
            log.fine("Could not find scope " + scopeName);
            return newHashSet();
        } else {
            log.fine("Returning endpoints for scope " + scopeName);
            return query.getSingleResult().getScopeEndpoints();
        }
    }
}
