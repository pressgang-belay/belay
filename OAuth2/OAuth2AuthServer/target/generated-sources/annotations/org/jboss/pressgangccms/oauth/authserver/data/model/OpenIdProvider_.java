package org.jboss.pressgangccms.oauth.authserver.data.model;

import java.math.BigInteger;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(OpenIdProvider.class)
public abstract class OpenIdProvider_ {

	public static volatile SingularAttribute<OpenIdProvider, String> providerUrl;
	public static volatile SingularAttribute<OpenIdProvider, String> providerName;
	public static volatile SingularAttribute<OpenIdProvider, BigInteger> providerId;

}

