package org.jboss.pressgang.belay.oauth2.gwt.script.client;

import com.google.gwt.core.client.EntryPoint;
import org.jboss.pressgang.belay.oauth2.gwt.client.Authorizer;

/**
 * This a renamed copy of the ScriptEntryPoint class from the gwt-oauth2-0.2-alpha library (http://code.google.com/p/gwt-oauth2/).
 * <p/>
 * This code is licensed under Apache License Version 2.0 (http://www.apache.org/licenses/LICENSE-2.0).
 * <p/>
 * An EntryPoint class that exports the {@link org.jboss.pressgang.belay.oauth2.gwt.client.Authorizer#authorize()} method.
 *
 * @author jasonhall@google.com (Jason Hall)
 */
public class OAuth2GwtClientProviderScriptEntryPoint implements EntryPoint {

    @Override
    public void onModuleLoad() {
        Authorizer.export();
    }
}
