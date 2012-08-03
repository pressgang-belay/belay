package org.jboss.pressgangccms.oauth2.resourceserver.service;

import com.google.common.base.Optional;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jboss.pressgangccms.oauth2.resourceserver.data.dao.OAuth2RSEndpointDao;
import org.jboss.pressgangccms.oauth2.resourceserver.data.dao.OAuth2RSScopeDao;
import org.jboss.pressgangccms.oauth2.resourceserver.data.model.OAuth2RSEndpoint;
import org.jboss.pressgangccms.oauth2.resourceserver.util.ResourceServer;
import org.jboss.pressgangccms.oauth2.shared.data.model.TokenGrantInfo;
import org.jboss.pressgangccms.oauth2.shared.rest.TokenGrantInfoEndpoint;
import org.jboss.resteasy.client.ClientExecutor;
import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.resteasy.client.core.executors.ApacheHttpClient4Executor;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.jboss.seam.solder.resourceLoader.Resource;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import static org.jboss.pressgangccms.oauth2.resourceserver.util.Common.PROPERTIES_FILEPATH;

/**
 * Service class wraps calls to DAOs and web services for auth information.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
@Stateless
public class OAuth2RSAuthService {

    @Inject
    @ResourceServer
    private Logger log;

    @Inject
    private OAuth2RSEndpointDao endpointDao;

    @Inject
    private OAuth2RSScopeDao scopeDao;

    @Inject
    @Resource(PROPERTIES_FILEPATH)
    private Properties resourceServerConfig;

    private static String authServerUsername;
    private static String authServerPassword;
    private static String authServerInfoUrl;

    public OAuth2RSAuthService() {
    }

    @PostConstruct
    private void initialise () {
        authServerUsername = (String) resourceServerConfig.get("authServerUsername");
        authServerPassword = (String) resourceServerConfig.get("authServerPassword");
        authServerInfoUrl = (String) resourceServerConfig.get("authServerInfoUrl");
    }

    public Optional<OAuth2RSEndpoint> getEndpointForRequest(HttpServletRequest request) {
        return endpointDao.findEndpointMatchingRequest(request);
    }

    public Optional<TokenGrantInfo> getTokenGrantInfoForAccessToken(final String accessToken) {
        log.info("Requesting token grant info from OAuth2 auth server");
        RegisterBuiltin.register(ResteasyProviderFactory.getInstance());  //TODO move this as only needs to be done once
        if (authServerUsername == null || authServerPassword == null || authServerInfoUrl == null) {
            log.severe("Resource server properties have not been set in the resourceserver.properties file");
            return Optional.absent();
        }
        AbstractHttpClient httpClient = new DefaultHttpClient();
        Credentials credentials = new UsernamePasswordCredentials(authServerUsername, authServerPassword);
        httpClient.getCredentialsProvider().setCredentials(AuthScope.ANY, credentials);
        ClientExecutor clientExecutor = new ApacheHttpClient4Executor(httpClient);
        TokenGrantInfoEndpoint client = ProxyFactory.create(TokenGrantInfoEndpoint.class, authServerInfoUrl, clientExecutor);
        TokenGrantInfo info = client.getTokenGrantInfoForAccessToken(accessToken);
        if (info != null) {
            return Optional.of(info);
        }
        return Optional.absent();
    }

    public Set<OAuth2RSEndpoint> getEndpointsForScopeName(String scopeName) {
        return scopeDao.findEndpointsForScopeName(scopeName);
    }
}
