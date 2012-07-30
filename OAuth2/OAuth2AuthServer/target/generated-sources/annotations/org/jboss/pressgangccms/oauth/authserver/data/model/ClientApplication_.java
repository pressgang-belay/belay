package org.jboss.pressgangccms.oauth.authserver.data.model;

import java.math.BigInteger;
import javax.annotation.Generated;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(ClientApplication.class)
public abstract class ClientApplication_ {

	public static volatile SetAttribute<ClientApplication, TokenGrant> tokenGrants;
	public static volatile SingularAttribute<ClientApplication, String> clientIdentifier;
	public static volatile SingularAttribute<ClientApplication, String> clientRedirectUri;
	public static volatile SingularAttribute<ClientApplication, String> clientName;
	public static volatile SingularAttribute<ClientApplication, String> clientSecret;
	public static volatile SingularAttribute<ClientApplication, BigInteger> clientId;

}

