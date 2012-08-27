package org.jboss.pressgangccms.openid.util;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import java.util.logging.Logger;

/**
 * This class uses CDI to alias Java EE resources, such as the persistence context, to CDI beans.
 */
public class Resources {
	@Produces
	public Logger produceLog(InjectionPoint injectionPoint) {
		return Logger.getLogger(injectionPoint.getMember().getDeclaringClass()
				.getName());
	}
}
