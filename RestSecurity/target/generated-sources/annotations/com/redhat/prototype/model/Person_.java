package com.redhat.prototype.model;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Person.class)
public abstract class Person_ {

	public static volatile SingularAttribute<Person, Long> personId;
	public static volatile SingularAttribute<Person, String> personUsername;
	public static volatile SingularAttribute<Person, String> personEmail;
	public static volatile SingularAttribute<Person, String> personName;
	public static volatile SingularAttribute<Person, String> personPassword;

}

