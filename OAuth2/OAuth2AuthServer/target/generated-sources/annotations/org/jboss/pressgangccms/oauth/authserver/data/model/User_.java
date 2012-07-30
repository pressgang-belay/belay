package org.jboss.pressgangccms.oauth.authserver.data.model;

import java.math.BigInteger;
import javax.annotation.Generated;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(User.class)
public abstract class User_ {

	public static volatile SetAttribute<User, Identity> userIdentities;
	public static volatile SingularAttribute<User, String> username;
	public static volatile SingularAttribute<User, BigInteger> userId;
	public static volatile SingularAttribute<User, Identity> primaryIdentity;

}

