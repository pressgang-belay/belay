package org.jboss.pressgang.belay.oauth2.authserver.util;

import org.jboss.seam.solder.resourceLoader.Resource;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import java.lang.reflect.Field;
import java.util.Properties;
import java.util.logging.Logger;

import static org.jboss.pressgang.belay.oauth2.authserver.util.Constants.ONE_HOUR;
import static org.jboss.pressgang.belay.oauth2.authserver.util.Constants.PROPERTIES_FILEPATH;

/**
 * This class uses CDI to alias Java EE resources, such as the persistence context, to CDI beans.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
public class Resources {
    @SuppressWarnings("unused")
    @Inject
    @Resource(PROPERTIES_FILEPATH)
    private Properties authServerConfig;

	@SuppressWarnings("unused")
	@Produces
    @AuthServer
    @PersistenceContext(unitName = "oauth2-authserver", type = PersistenceContextType.EXTENDED)
	private EntityManager em;

	@Produces
    @AuthServer
	public Logger produceLog(InjectionPoint injectionPoint) {
		return Logger.getLogger(injectionPoint.getMember().getDeclaringClass()
				.getName());
	}

    @PostConstruct
    private void initialise() {
        if (authServerConfig != null) {
            for (Field field : Resources.class.getFields()) {
                String propertyValue = (String) authServerConfig.get(field.getName());
                if (propertyValue != null && (! propertyValue.isEmpty())) {
                    try {
                        field.set(this, propertyValue);
                        log.fine("Set AuthServer " + field.getName() + " property to: " + propertyValue);
                    } catch (IllegalAccessException e) {
                        log.severe("Could not set AuthServer property: " + field.getName());
                    }
                }
            }
        }
    }

    private Logger log = Logger.getLogger(Resources.class.getName());
    public static String oAuthTokenExpiry = ONE_HOUR;
    public static String urlEncoding = "UTF-8";
    public static String openIdRealm = "/OAuth2AuthServer/rest/auth/";
    public static String authEndpoint = "/auth/authorise";
    public static String restEndpointBasePath = "/OAuth2AuthServer/rest";
    public static String completeAssociationEndpoint = "/auth/user/associate/completeAssociation";
    public static String openIdReturnUri = restEndpointBasePath + authEndpoint;
    public static String authServerOAuthClientId = "OAuth2AuthServer";
    public static String defaultScopeName = "DEFAULT";
    public static String promptEndUserToApproveClientAppOnEveryLogin = "false";
    public static String endUserConsentUri = "/auth/consent";
    public static String endUserConsentFormCssLocation = "";
}
