package org.jboss.pressgangccms.oauth.authserver.data.model;

import java.math.BigInteger;
import javax.annotation.Generated;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Identity.class)
public abstract class Identity_ {

	public static volatile SingularAttribute<Identity, OpenIdProvider> openIdProvider;
	public static volatile SingularAttribute<Identity, String> lastName;
	public static volatile SetAttribute<Identity, Scope> identityScopes;
	public static volatile SetAttribute<Identity, TokenGrant> tokenGrants;
	public static volatile SingularAttribute<Identity, BigInteger> identityId;
	public static volatile SingularAttribute<Identity, String> email;
	public static volatile SingularAttribute<Identity, String> language;
	public static volatile SingularAttribute<Identity, String> fullName;
	public static volatile SingularAttribute<Identity, String> firstName;
	public static volatile SingularAttribute<Identity, User> user;
	public static volatile SingularAttribute<Identity, String> identifier;
	public static volatile SingularAttribute<Identity, String> country;

}

