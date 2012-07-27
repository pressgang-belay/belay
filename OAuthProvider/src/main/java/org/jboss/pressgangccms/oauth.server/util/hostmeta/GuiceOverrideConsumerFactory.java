package org.jboss.pressgangccms.oauth.server.util.hostmeta;

import com.google.code.openid.ConsumerFactory;
import com.google.code.openid.GuiceModule;
import com.google.inject.*;
import com.google.inject.util.Modules;
import com.google.step2.ConsumerHelper;
import com.google.step2.discovery.Discovery2;
import com.google.step2.discovery.HostMetaFetcher;
import com.google.step2.discovery.UrlHostMetaFetcher;
import com.google.step2.http.DefaultHttpFetcher;
import com.google.step2.http.HttpFetcher;
import org.mortbay.log.Log;
import org.openid4java.consumer.ConsumerAssociationStore;
import org.openid4java.consumer.InMemoryConsumerAssociationStore;
import org.openid4java.discovery.Discovery;

import java.util.logging.Logger;

public class GuiceOverrideConsumerFactory extends ConsumerFactory {

    @Inject
    private Logger log;

    ConsumerHelper helper;

    public GuiceOverrideConsumerFactory() {
        this(new InMemoryConsumerAssociationStore());
    }

    public GuiceOverrideConsumerFactory(ConsumerAssociationStore store) {
        Log.info("Overriding Guice injection");
        Injector injector = Guice.createInjector(Modules.override(new GuiceModule(store)).with(new GuiceOverrideModule()));
        helper = injector.getInstance(ConsumerHelper.class);
    }

    public ConsumerHelper getConsumerHelper() {
        return helper;
    }

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
