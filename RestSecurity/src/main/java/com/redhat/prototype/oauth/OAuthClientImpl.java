package com.redhat.prototype.oauth;

import com.redhat.prototype.Common;
import org.apache.amber.oauth2.rsfilter.OAuthClient;

public class OAuthClientImpl implements OAuthClient {

    private String clientId;

    public OAuthClientImpl(String clientId) {
        this.clientId = clientId;    //TODO lookup client by id
    }

    @Override
    public String getClientId() {
        return clientId;
    }
}
