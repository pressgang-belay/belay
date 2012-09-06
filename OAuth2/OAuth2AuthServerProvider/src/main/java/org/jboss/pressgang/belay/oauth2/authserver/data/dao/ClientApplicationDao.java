package org.jboss.pressgang.belay.oauth2.authserver.data.dao;

import com.google.common.base.Optional;
import org.jboss.pressgang.belay.oauth2.authserver.data.model.ClientApplication;
import org.jboss.pressgang.belay.oauth2.authserver.util.AuthServer;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.logging.Logger;

/**
 * ClientApplication DAO.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
@ApplicationScoped
public class ClientApplicationDao {

    @Inject
    @AuthServer
    private EntityManager em;

    @Inject
    @AuthServer
    private Logger log;

    public Optional<ClientApplication> getClientApplicationFromClientIdentifier(String clientIdentifier) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ClientApplication> criteria = cb.createQuery(ClientApplication.class);
        Root<ClientApplication> clientApplication = criteria.from(ClientApplication.class);
        criteria.select(clientApplication).where(cb.equal(clientApplication.get("clientIdentifier"),
                clientIdentifier));
        TypedQuery<ClientApplication> query = em.createQuery(criteria);
        if (query.getResultList().size() == 1) {
            log.fine("Returning ClientApplication with identifier " + clientIdentifier);
            return Optional.of(query.getSingleResult());
        } else {
            log.fine("Could not find ClientApplication with identifier " + clientIdentifier);
            return Optional.absent();
        }
    }
}
