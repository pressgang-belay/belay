package org.jboss.pressgang.belay.oauth2.resourceserver.util;

import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.jboss.seam.solder.resourceLoader.Resource;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.Properties;
import java.util.logging.Logger;

import static org.jboss.pressgang.belay.oauth2.resourceserver.util.Constants.PROPERTIES_FILEPATH;

/**
 * This class uses CDI to alias Java EE resources, such as the persistence context, to CDI beans.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
public class Resources {
    @Inject
    @Resource(PROPERTIES_FILEPATH)
    private Properties resourceServerConfig;

    private Logger log = Logger.getLogger(Resources.class.getName());
    private final static String DEFAULT_AUTH_SERVICE_JNDI_ADDRESS = "java:module/OAuth2RSAuthService";
    private static String authServiceJndiAddress;
    private static String entityManagerFactoryJndiAddress;
    private static String tokenExpiryExtensionThreshold; // In seconds
    private static final String TOKEN_EXPIRY_THRESHOLD_DEFAULT = "900";
    private static int tokenExpiryThresholdSeconds;
    private static EntityManagerFactory emf;
    private static EntityManager em;

    @PostConstruct
    private void initialize() {
        entityManagerFactoryJndiAddress = (String) resourceServerConfig.get("entityManagerFactoryJndiAddress");
        log.fine("Found entityManagerFactoryJndiAddress property: " + entityManagerFactoryJndiAddress);

        // Make sure RestEasy provider factory is registered
        RegisterBuiltin.register(ResteasyProviderFactory.getInstance());

        tokenExpiryExtensionThreshold = (String)  resourceServerConfig.get("tokenExpiryExtensionThreshold");
        try {
        tokenExpiryThresholdSeconds = tokenExpiryExtensionThreshold == null || tokenExpiryExtensionThreshold.isEmpty()
                ? Integer.parseInt(TOKEN_EXPIRY_THRESHOLD_DEFAULT)
                : Integer.parseInt(tokenExpiryExtensionThreshold);
        } catch (NumberFormatException e) {
            log.severe("Could not set token expiry extension threshold property");
            throw new RuntimeException("Error: Resource server properties set incorrectly");
        }
        authServiceJndiAddress = (resourceServerConfig.get("authServiceJndiAddress") == null)
                ? DEFAULT_AUTH_SERVICE_JNDI_ADDRESS
                : (String) resourceServerConfig.get("authServiceJndiAddress");
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

    public static int getTokenExpiryExtensionThreshold() {
        return tokenExpiryThresholdSeconds;
    }

    public static String getAuthServiceJndiAddress() {
        return authServiceJndiAddress;
    }
}
