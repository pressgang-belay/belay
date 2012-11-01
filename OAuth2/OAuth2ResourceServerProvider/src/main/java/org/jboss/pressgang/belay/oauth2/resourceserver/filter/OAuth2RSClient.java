package org.jboss.pressgang.belay.oauth2.resourceserver.filter;

import org.apache.amber.oauth2.rsfilter.OAuthClient;

import java.lang.Override;
import java.lang.String;

/**
 * Implementation of OAuthClient for use during OAuth filtering.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
public class OAuth2RSClient implements OAuthClient {

    private String clientIdentifier;

    public OAuth2RSClient(String clientIdentifier) {
        this.clientIdentifier = clientIdentifier;
    }

    @Override
    public String getClientId() {
        return clientIdentifier;
    }
}
