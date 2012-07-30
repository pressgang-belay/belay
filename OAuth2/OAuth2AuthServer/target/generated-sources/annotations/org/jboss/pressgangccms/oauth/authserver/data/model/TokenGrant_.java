package org.jboss.pressgangccms.oauth.authserver.data.model;

import java.math.BigInteger;
import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(TokenGrant.class)
public abstract class TokenGrant_ {

	public static volatile SingularAttribute<TokenGrant, ClientApplication> grantClient;
	public static volatile SingularAttribute<TokenGrant, String> accessToken;
	public static volatile SingularAttribute<TokenGrant, Date> grantTimeStamp;
	public static volatile SingularAttribute<TokenGrant, BigInteger> tokenGrantId;
	public static volatile SingularAttribute<TokenGrant, Identity> grantIdentity;
	public static volatile SingularAttribute<TokenGrant, Boolean> grantCurrent;
	public static volatile SingularAttribute<TokenGrant, String> refreshToken;
	public static volatile SingularAttribute<TokenGrant, String> accessTokenExpiry;
	public static volatile SetAttribute<TokenGrant, Scope> grantScopes;

}

