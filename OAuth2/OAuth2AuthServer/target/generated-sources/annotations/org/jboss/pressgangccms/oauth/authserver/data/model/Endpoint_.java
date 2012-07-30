package org.jboss.pressgangccms.oauth.authserver.data.model;

import com.google.appengine.api.urlfetch.HTTPMethod;
import java.math.BigInteger;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Endpoint.class)
public abstract class Endpoint_ {

	public static volatile SingularAttribute<Endpoint, HTTPMethod> endpointMethod;
	public static volatile SingularAttribute<Endpoint, String> endpointUrlPattern;
	public static volatile SingularAttribute<Endpoint, BigInteger> endpointId;

}

