package com.redhat.prototype.oauth.request;

import com.redhat.prototype.model.auth.ClientApplication;
import org.apache.amber.oauth2.rsfilter.OAuthClient;

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
