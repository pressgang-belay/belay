package org.jboss.pressgangccms.oauth2.authserver.openid;

import com.google.code.openid.ConsumerFactory;
import com.google.code.openid.GuiceModule;
import com.google.inject.*;
import com.google.inject.util.Modules;
import com.google.step2.ConsumerHelper;
import com.google.step2.discovery.HostMetaFetcher;
import org.mortbay.log.Log;
import org.openid4java.consumer.ConsumerAssociationStore;
import org.openid4java.consumer.InMemoryConsumerAssociationStore;

import java.util.logging.Logger;

/**
 * The purpose of this class is to override the Guice binding of HostMetaFetcher to the GoogleHostedHostMetaFetcher
 * in the openid-filter library. This increases efficiency by preventing unnecessary calls to Google, and prevents
 * the disclosure of URLs that may be internal, such as the URL of an internal OpenID provider.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
public class GuiceOverrideConsumerFactory extends ConsumerFactory {

    @Inject
    private Logger log;

    ConsumerHelper helper;

    public GuiceOverrideConsumerFactory() {
        this(new InMemoryConsumerAssociationStore());
    }

    public GuiceOverrideConsumerFactory(ConsumerAssociationStore store) {
        Log.info("Overriding Guice binding");
        Injector injector = Guice.createInjector(Modules.override(new GuiceModule(store)).with(new GuiceOverrideModule()));
        helper = injector.getInstance(ConsumerHelper.class);
    }

    public ConsumerHelper getConsumerHelper() {
        return helper;
    }

    /**
     * Class to override the existing GuiceModule.
     */
    public class GuiceOverrideModule implements Module {
        @Override
        public void configure(Binder binder) {
            Log.info("Configuring binding for CustomHostMetaFetcher");
            binder.bind(HostMetaFetcher.class)
                    .to(CustomHostMetaFetcher.class)
                    .in(Scopes.SINGLETON);
        }
    }
}
