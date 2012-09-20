package org.jboss.pressgang.belay.oauth2.resourceserver.service;

import com.google.common.base.Optional;
import org.jboss.pressgang.belay.oauth2.resourceserver.data.model.OAuth2RSEndpoint;
import org.jboss.pressgang.belay.oauth2.shared.data.model.AccessTokenExpiryInfo;
import org.jboss.pressgang.belay.oauth2.shared.data.model.TokenGrantInfo;

import javax.servlet.http.HttpServletRequest;
import java.util.Set;

/**
 * @author kamiller@redhat.com (Katie Miller)
 */
public interface OAuth2RSAuthService {
    public Optional<OAuth2RSEndpoint> getEndpointForRequest(HttpServletRequest request);

    public Set<OAuth2RSEndpoint> getEndpointsForScopeName(String scopeName);

    public Optional<TokenGrantInfo> getTokenGrantInfoForAccessToken(final String accessToken);

    public Optional<AccessTokenExpiryInfo> extendAccessTokenExpiry(final String accessToken);
}
