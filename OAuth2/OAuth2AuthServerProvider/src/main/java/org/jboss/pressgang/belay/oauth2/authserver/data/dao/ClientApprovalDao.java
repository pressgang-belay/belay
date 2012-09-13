package org.jboss.pressgang.belay.oauth2.authserver.data.dao;

import org.jboss.pressgang.belay.oauth2.authserver.data.model.ClientApproval;
import org.jboss.pressgang.belay.oauth2.authserver.util.AuthServer;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.logging.Logger;

/**
 * Client Approval DAO.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
public class ClientApprovalDao {

    @Inject
    @AuthServer
    private EntityManager em;

    @Inject
    @AuthServer
    private Logger log;

    @Inject
    private Event<ClientApproval> clientApprovalEventSrc;

    public void addClientApproval(ClientApproval clientApproval) {
        log.info("Registering client approval");
        em.persist(clientApproval);
        clientApprovalEventSrc.fire(clientApproval);
    }

    public void update(ClientApproval clientApproval) {
        log.info("Updating client approval");
        em.merge(clientApproval);
        clientApprovalEventSrc.fire(clientApproval);
    }
}
