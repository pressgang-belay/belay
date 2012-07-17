package org.jboss.pressgangccms.oauth.server.oauth.request;

import org.jboss.pressgangccms.oauth.server.data.model.auth.ClientApplication;
import org.apache.amber.oauth2.rsfilter.OAuthClient;

/**
 * Implementation of OAuthClient for use during OAuth filtering.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
public class OAuthClientImpl implements OAuthClient {

    private ClientApplication client;

    public OAuthClientImpl(ClientApplication client) {
        this.client = client;
    }

    @Override
    public String getClientId() {
        return client.getClientIdentifier();
    }
}
