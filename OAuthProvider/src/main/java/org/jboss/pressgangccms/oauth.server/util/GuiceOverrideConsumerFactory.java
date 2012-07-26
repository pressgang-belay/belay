package org.jboss.pressgangccms.oauth.server.util;

import com.google.code.openid.ConsumerFactory;
import com.google.code.openid.GuiceModule;
import com.google.inject.*;
import com.google.inject.util.Modules;
import com.google.step2.ConsumerHelper;
import com.google.step2.discovery.DefaultHostMetaFetcher;
import com.google.step2.discovery.HostMetaFetcher;
import org.openid4java.consumer.ConsumerAssociationStore;
import org.openid4java.consumer.InMemoryConsumerAssociationStore;

public class GuiceOverrideConsumerFactory extends ConsumerFactory {

    ConsumerHelper helper;

    public GuiceOverrideConsumerFactory() {
        this(new InMemoryConsumerAssociationStore());
    }

    public GuiceOverrideConsumerFactory(ConsumerAssociationStore store) {
        Module libraryModule = new GuiceModule(store);
        Module newModule = new GuiceOverrideModule();
        Injector injector = Guice.createInjector(Modules.override(libraryModule).with(newModule));
        helper = injector.getInstance(ConsumerHelper.class);
    }

    public ConsumerHelper getConsumerHelper() {
        return helper;
    }

    public class GuiceOverrideModule implements Module {
        @Override
        public void configure(Binder binder) {
            binder.bind(HostMetaFetcher.class)
                    .to(DefaultHostMetaFetcher.class)
                    .in(Scopes.SINGLETON);
        }
    }
}
