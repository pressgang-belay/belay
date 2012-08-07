package org.jboss.pressgangccms.oauth2.resourceserver.util;

import org.jboss.seam.solder.resourceLoader.Resource;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.Properties;
import java.util.logging.Logger;

import static org.jboss.pressgangccms.oauth2.resourceserver.util.Common.PROPERTIES_FILEPATH;

/**
 * This class uses CDI to alias Java EE resources, such as the persistence
 * context, to CDI beans
 */
public class Resources {
    @Inject
    @Resource(PROPERTIES_FILEPATH)
    private Properties resourceServerConfig;

    private Logger log = Logger.getLogger(Resources.class.getName());
    private static String entityManagerFactoryJndiAddress;
    private static EntityManagerFactory emf;
    private static EntityManager em;

    @PostConstruct
    private void initialise() {
        entityManagerFactoryJndiAddress = (String) resourceServerConfig.get("entityManagerFactoryJndiAddress");
        log.fine("Found entityManagerFactoryJndiAddress property: " + entityManagerFactoryJndiAddress);
    }

    @SuppressWarnings("unused")
    @Produces
    @ResourceServer
    private EntityManager produceEntityManager() {
        try {
            if (emf == null) {
                log.fine("Looking up entity manager factory via JNDI");
                emf = (EntityManagerFactory) new InitialContext().lookup(entityManagerFactoryJndiAddress);
            }
        } catch (NamingException e) {
            log.severe("Could not perform JNDI lookup for resource server entity manager factory: " + e);
        }
        if (em == null) {
            em = emf.createEntityManager();
        }
        if (em != null) {
            log.fine("Returning entity manager");
            return em;
        }
        log.severe("Could not create resource server entity manager");
        throw new RuntimeException("Could not create resource server entity manager");
    }

    @Produces
    @ResourceServer
    public Logger produceLog(InjectionPoint injectionPoint) {
        return Logger.getLogger(injectionPoint.getMember().getDeclaringClass()
                .getName());
    }
}
