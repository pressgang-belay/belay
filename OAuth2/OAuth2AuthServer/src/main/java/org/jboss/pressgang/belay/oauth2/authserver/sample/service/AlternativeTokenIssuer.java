package org.jboss.pressgang.belay.oauth2.authserver.sample.service;

import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.jboss.pressgang.belay.oauth2.authserver.service.TokenIssuer;

import javax.ejb.Stateless;
import javax.enterprise.inject.Alternative;

/**
 * Sample class demonstrating how an alternative TokenIssuer could be introduced.
 * Uncomment the 'alternatives' section in the sample beans.xml to enable this implementation
 * and configure it to be used instead of the default from the AuthServerProvider library.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
@Alternative
@Stateless
public class AlternativeTokenIssuer implements TokenIssuer {

    @Override
    public String accessToken() throws OAuthSystemException {
        return "foo";
    }

    @Override
    public String refreshToken() throws OAuthSystemException {
        return "bar";
    }

    @Override
    public String authorizationCode() throws OAuthSystemException {
        return "foobar";
    }
}