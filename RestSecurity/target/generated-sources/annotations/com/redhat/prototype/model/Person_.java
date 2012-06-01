package com.redhat.prototype.model;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Person.class)
public abstract class Person_ {

	public static volatile SingularAttribute<Person, Long> id;
	public static volatile SingularAttribute<Person, String> username;
	public static volatile SingularAttribute<Person, String> email;
	public static volatile SingularAttribute<Person, String> name;
	public static volatile SingularAttribute<Person, String> password;

}

