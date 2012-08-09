package org.jboss.pressgangccms.oauth2.authserver.util;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import java.util.logging.Logger;

/**
 * This class uses CDI to alias Java EE resources, such as the persistence
 * context, to CDI beans
 */
public class Resources {
	// use @SuppressWarnings to tell IDE to ignore warnings about field not
	// being referenced directly
	@SuppressWarnings("unused")
	@Produces
    @PersistenceContext(unitName = "oauth2-authserver", type = PersistenceContextType.EXTENDED)
	private EntityManager em;

	@Produces
	public Logger produceLog(InjectionPoint injectionPoint) {
		return Logger.getLogger(injectionPoint.getMember().getDeclaringClass()
				.getName());
	}
}
