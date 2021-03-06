package org.jboss.pressgang.belay.oauth2.resourceserver.data.dao;

import com.google.common.base.Optional;
import org.jboss.pressgang.belay.oauth2.resourceserver.data.model.OAuth2RSEndpoint;
import org.jboss.pressgang.belay.oauth2.resourceserver.util.ResourceServer;

import javax.enterprise.context.RequestScoped;
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
 * Resource server endpoint DAO.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
@RequestScoped
public class OAuth2RSEndpointDao {

    @Inject
    @ResourceServer
    private Logger log;

    @Inject
    @ResourceServer
    private EntityManager em;

    public Optional<OAuth2RSEndpoint> findEndpointMatchingRequest(final HttpServletRequest request) {
        String requestUrl = request.getRequestURL().toString();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<OAuth2RSEndpoint> criteria = cb.createQuery(OAuth2RSEndpoint.class);
        Root<OAuth2RSEndpoint> endpoint = criteria.from(OAuth2RSEndpoint.class);
        criteria.select(endpoint).where(cb.isFalse(endpoint.get("urlRegularExpression").as(Boolean.class)),
                                                        cb.equal(endpoint.get("endpointUrl"), requestUrl));
        TypedQuery<OAuth2RSEndpoint> query = em.createQuery(criteria);
        if (query.getResultList().size() > 0) {
            // Found at least one exact match for endpoint
            if (query.getResultList().size() == 1) {
                // Just one match so check its method directly
                OAuth2RSEndpoint e = query.getSingleResult();
                if (methodMatches(request, e)) {
                    log.fine("Returning Endpoint with URL " + e.getEndpointUrl()
                            + " and method " + e.getEndpointMethod());
                    return Optional.of(e);
                }
            } else {
                // Multiple matches, so find the one with the matching method
                for (OAuth2RSEndpoint e : query.getResultList()) {
                    if (e.getEndpointMethod() != null && endpointMethodMatchesRequest(request, e.getEndpointMethod())) {
                        log.fine("Returning Endpoint with URL " + e.getEndpointUrl()
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
            log.fine("Checking URL against endpoint regular expressions");
            // No exact match for endpoint, so check if it matches a pattern
            // Find all endpoints, ordered by URL pattern
            cb = em.getCriteriaBuilder();
            criteria = cb.createQuery(OAuth2RSEndpoint.class);
            endpoint = criteria.from(OAuth2RSEndpoint.class);
            criteria.select(endpoint).where(cb.isTrue(endpoint.get("urlRegularExpression").as(Boolean.class)));
            List<OAuth2RSEndpoint> resultList = em.createQuery(criteria).getResultList();
            for (OAuth2RSEndpoint e : resultList) {
                log.fine("Checking for match with endpoint pattern: " + e.getEndpointUrl());
                if (requestUrl.matches(e.getEndpointUrl())
                        && methodMatches(request, e)) {
                    log.fine("Returning endpoint with URL pattern " + e.getEndpointUrl()
                            + " and method " + e.getEndpointMethod() + " to match request URL "
                            + requestUrl);
                    return Optional.of(e);
                }
            }
        }
        log.info("Could not find matching endpoint mapping for: " + requestUrl);
        return Optional.absent();
    }

    private boolean methodMatches(HttpServletRequest request, OAuth2RSEndpoint endpoint) {
        return endpoint.getEndpointMethod() == null
                || endpointMethodMatchesRequest(request, endpoint.getEndpointMethod());
    }

    private boolean endpointMethodMatchesRequest(HttpServletRequest request, String method) {
        return request.getMethod().equalsIgnoreCase(method);
    }
}
