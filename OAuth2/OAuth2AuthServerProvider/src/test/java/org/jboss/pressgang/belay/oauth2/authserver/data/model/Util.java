package org.jboss.pressgang.belay.oauth2.authserver.data.model;

/**
 * @author kamiller@redhat.com (Katie Miller)
 */
public class Util {

    public static User makeUser() {
        User one = new User();
        one.setPrimaryIdentity(makeIdentity());
        return one;
    }

    public static User makeDifferentUser() {
        User two = new User();
        two.setPrimaryIdentity(makeDifferentIdentity());
        return two;
    }

    public static Identity makeIdentity() {
        Identity one = new Identity();
        one.setIdentifier("http://jsmith.myopenid.com");
        return one;
    }

    public static Identity makeDifferentIdentity() {
        Identity two = new Identity();
        two.setIdentifier("http://jdoe.myopenid.com");
        return two;
    }

    public static OpenIdProvider makeOpenIdProvider() {
        OpenIdProvider openIdProvider = new OpenIdProvider();
        openIdProvider.setProviderName("Test Provider");
        openIdProvider.setProviderUrl("http://www.test.com");
        return openIdProvider;
    }
}
