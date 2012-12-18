package org.jboss.pressgang.belay.oauth2.sample.service;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import org.jboss.pressgang.belay.oauth2.resourceserver.data.model.OAuth2RSEndpoint;
import org.jboss.pressgang.belay.oauth2.resourceserver.service.OAuth2RSAuthService;
import org.jboss.pressgang.belay.oauth2.shared.data.model.AccessTokenExpiryInfo;
import org.jboss.pressgang.belay.oauth2.shared.data.model.TokenGrantInfo;

import javax.ejb.Stateless;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Sample class demonstrating how an alternative OAuth2RSAuthService could be introduced.
 * Uncomment the 'alternatives' section in the sample beans.xml to enable this implementation
 * and configure it to be used instead of the default from the ResourceServerProvider library.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
@Alternative
@Stateless
public class AlternativeOAuth2RSAuthService implements OAuth2RSAuthService {

    @Inject
    private Logger log;

    @Override
    public Optional<OAuth2RSEndpoint> getEndpointForRequest(HttpServletRequest httpServletRequest) {
        log.info("AlternativeOAuth2RSAuthService's getEndpointForRequest method called");
        return Optional.absent();
    }

    @Override
    public Set<OAuth2RSEndpoint> getEndpointsForScopeName(String s) {
        log.info("AlternativeOAuth2RSAuthService's getEndpointsForScopeName method called");
        return Sets.newHashSet();
    }

    @Override
    public Optional<TokenGrantInfo> getTokenGrantInfoByAccessToken(String s) {
        log.info("AlternativeOAuth2RSAuthService's getTokenGrantInfoForAccessToken method called");
        return Optional.absent();
    }

    @Override
    public Optional<AccessTokenExpiryInfo> extendAccessTokenExpirySeconds(String s) {
        log.info("AlternativeOAuth2RSAuthService's extendAccessTokenExpiry method called");
        return Optional.absent();
    }
}
