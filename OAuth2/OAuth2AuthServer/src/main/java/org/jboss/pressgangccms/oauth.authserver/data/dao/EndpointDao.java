package org.jboss.pressgangccms.oauth.authserver.data.dao;

import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.repackaged.com.google.common.base.Optional;
import org.jboss.pressgangccms.oauth.authserver.data.model.Endpoint;
import org.jboss.pressgangccms.oauth.authserver.data.model.Endpoint_;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.logging.Logger;

/**
 * Endpoint DAO.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
@ApplicationScoped
public class EndpointDao {

    @Inject
    private EntityManager em;

    @Inject
    private Logger log;

    public Optional<Endpoint> findEndpointMatchingRequest(HttpServletRequest request) {
        String requestUrl = request.getRequestURL().toString();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Endpoint> criteria = cb.createQuery(Endpoint.class);
        Root<Endpoint> endpoint = criteria.from(Endpoint.class);
        criteria.select(endpoint).where(cb.equal(endpoint.get(Endpoint_.endpointUrlPattern),
                requestUrl));
        TypedQuery<Endpoint> query = em.createQuery(criteria);
        if (query.getResultList().size() > 0) {
            // Found at least one exact match for endpoint
            if (query.getResultList().size() == 1) {
                // Just one match so check its method directly
                Endpoint e = query.getSingleResult();
                if (methodMatches(request, e)) {
                    log.fine("Returning Endpoint with URL " + e.getEndpointUrlPattern()
                            + " and method " + e.getEndpointMethod());
                    return Optional.of(e);
                }
            } else {
                // Multiple matches, so find the one with the matching method
                for (Endpoint e : query.getResultList()) {
                    if (e.getEndpointMethod() != null && endpointMethodMatchesRequest(request, e.getEndpointMethod())) {
                        log.fine("Returning Endpoint with URL " + e.getEndpointUrlPattern()
                                + " method " + e.getEndpointMethod());
                        return Optional.of(e);
                    }
                }
                // No matches so nothing to return
                log.fine("Could not find Endpoint with URL " + requestUrl
                        + " and method " + request.getMethod());
                return Optional.absent();
            }
        } else {
            log.fine("Checking URL against endpoint patterns");
            // No exact match for endpoint, so check if it matches a pattern
            // Find all endpoints, ordered by URL pattern
            cb = em.getCriteriaBuilder();
            criteria = cb.createQuery(Endpoint.class);
            endpoint = criteria.from(Endpoint.class);
            criteria.select(endpoint).orderBy(cb.asc(endpoint.get(Endpoint_.endpointUrlPattern)));
            List<Endpoint> resultList = em.createQuery(criteria).getResultList();
            for (Endpoint e : resultList) {
                if (requestUrl.matches(e.getEndpointUrlPattern())
                        && methodMatches(request, e)) {
                    log.fine("Returning Endpoint with URL pattern " + e.getEndpointUrlPattern()
                            + " and method " + e.getEndpointMethod() + " to match request URL "
                            + requestUrl);
                    return Optional.of(e);
                }
            }
        }
        log.info("Could not find matching Endpoint");
        return Optional.absent();
    }

    private boolean methodMatches(HttpServletRequest request, Endpoint endpoint) {
        return endpoint.getEndpointMethod() == null
                || endpointMethodMatchesRequest(request, endpoint.getEndpointMethod());
    }

    private boolean endpointMethodMatchesRequest(HttpServletRequest request, HTTPMethod method) {
        return request.getMethod().equals(method.toString());
    }
}
