package org.jboss.pressgangccms.oauth.authserver.oauth.request;

import org.jboss.pressgangccms.oauth.authserver.data.model.ClientApplication;
import org.apache.amber.oauth2.rsfilter.OAuthClient;

/**
 * Implementation of OAuthClient for use during OAuth filtering.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
public class OAuthClientImpl implements OAuthClient {

    private String clientIdentifier;

    public OAuthClientImpl(String clientIdentifier) {
        this.clientIdentifier = clientIdentifier;
    }

    @Override
    public String getClientId() {
        return clientIdentifier;
    }
}
