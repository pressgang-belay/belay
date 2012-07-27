package org.jboss.pressgangccms.oauth.server.util.hostmeta;

import com.google.code.openid.GoogleHostedHostMetaFetcher;
import com.google.inject.Inject;
import com.google.step2.discovery.DefaultHostMetaFetcher;
import com.google.step2.discovery.UrlHostMetaFetcher;
import com.google.step2.http.HttpFetcher;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;

public class CustomHostMetaFetcher extends UrlHostMetaFetcher {

    private Logger log = Logger.getLogger(CustomHostMetaFetcher.class.getName());
    private ExtendedDefaultHostMetaFetcher defaultHostMetaFetcher;
    private ExtendedGoogleHostedHostMetaFetcher googleHostedHostMetaFetcher;

    @Inject
    protected CustomHostMetaFetcher(HttpFetcher fetcher) {
        super(fetcher);
        defaultHostMetaFetcher = new ExtendedDefaultHostMetaFetcher(fetcher);
        googleHostedHostMetaFetcher = new ExtendedGoogleHostedHostMetaFetcher(fetcher);
    }

    @Override
    protected URI getHostMetaUriForHost(String host) throws URISyntaxException {
        if (host.contains("gmail") || host.contains("google")) {
            log.info("Using GoogleHostedMetaFetcher");
            return googleHostedHostMetaFetcher.getHostMetaUriForHost(host);
        } else {
            log.info("Using DefaultHostMetaFetcher");
            return defaultHostMetaFetcher.getHostMetaUriForHost(host);
        }
    }

    /**
     * Extended class so protected method can be accessed.
     */
    public class ExtendedGoogleHostedHostMetaFetcher extends GoogleHostedHostMetaFetcher {

        public ExtendedGoogleHostedHostMetaFetcher(HttpFetcher fetcher) {
            super(fetcher);
        }

        @Override
        protected URI getHostMetaUriForHost(String host) throws URISyntaxException {
            return super.getHostMetaUriForHost(host);
        }
    }

    /**
     * Extended class so protected method can be accessed.
     */
    public class ExtendedDefaultHostMetaFetcher extends DefaultHostMetaFetcher {

        public ExtendedDefaultHostMetaFetcher(HttpFetcher fetcher) {
            super(fetcher);
        }

        @Override
        protected URI getHostMetaUriForHost(String host) throws URISyntaxException {
            return super.getHostMetaUriForHost(host);
        }
    }
}
