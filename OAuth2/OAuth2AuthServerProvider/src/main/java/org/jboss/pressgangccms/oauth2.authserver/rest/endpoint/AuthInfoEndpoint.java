package org.jboss.pressgangccms.oauth2.authserver.rest.endpoint;

import org.jboss.pressgangccms.oauth2.shared.rest.TokenExpiryExtensionEndpoint;
import org.jboss.pressgangccms.oauth2.shared.rest.TokenGrantInfoEndpoint;

/**
 * @author kamiller@redhat.com (Katie Miller)
 */
public interface AuthInfoEndpoint extends TokenGrantInfoEndpoint, TokenExpiryExtensionEndpoint {
}
