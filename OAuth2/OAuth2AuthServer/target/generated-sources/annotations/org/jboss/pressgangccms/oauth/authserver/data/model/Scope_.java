package org.jboss.pressgangccms.oauth.authserver.data.model;

import java.math.BigInteger;
import javax.annotation.Generated;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Scope.class)
public abstract class Scope_ {

	public static volatile SetAttribute<Scope, Endpoint> scopeEndpoints;
	public static volatile SingularAttribute<Scope, BigInteger> scopeId;
	public static volatile SingularAttribute<Scope, String> scopeName;

}

