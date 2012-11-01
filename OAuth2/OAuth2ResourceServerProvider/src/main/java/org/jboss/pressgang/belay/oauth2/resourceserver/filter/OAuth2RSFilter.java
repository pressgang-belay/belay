package org.jboss.pressgang.belay.oauth2.resourceserver.filter;

import org.apache.amber.oauth2.rsfilter.OAuthFilter;
import org.apache.amber.oauth2.rsfilter.OAuthUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Filter for incoming requests. Allows HttpServletResponse to accessible during decision-making, so headers can
 * be added when necessary.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
public class OAuth2RSFilter extends OAuthFilter {

    private OAuth2RSProvider provider;

    public OAuth2RSFilter() {
        super();
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        provider = OAuthUtils
                .initiateServletContext(filterConfig.getServletContext(), OAUTH_RS_PROVIDER_CLASS,
                        OAuth2RSProvider.class);
        super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {
        provider.setServletResponse((HttpServletResponse) response);
        super.doFilter(request, response, chain);
    }
}
