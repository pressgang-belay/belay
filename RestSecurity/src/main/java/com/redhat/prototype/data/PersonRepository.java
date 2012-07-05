package com.redhat.prototype.data;

import com.redhat.prototype.model.Person;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

@ApplicationScoped
public class PersonRepository {

    @Inject
	private EntityManager em;

	public Person findById(Long id) {
		return em.find(Person.class, id);
	}

	public Person findByEmail(String email) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Person> criteria = cb.createQuery(Person.class);
		Root<Person> person = criteria.from(Person.class);
		// Swap criteria statements if you would like to try out type-safe
		// criteria queries, a new
		// feature in JPA 2.0
		// criteria.select(Person).where(cb.equal(Person.get(Person_.name),
		// email));
		criteria.select(person).where(cb.equal(person.get("personEmail"), email));
		return em.createQuery(criteria).getSingleResult();
	}
	
	public Person findByUsername(String username) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Person> criteria = cb.createQuery(Person.class);
		Root<Person> person = criteria.from(Person.class);
		criteria.select(person).where(cb.equal(person.get("personUsername"), username));
		return em.createQuery(criteria).getSingleResult();
	}

	public List<Person> findAllOrderedByName() {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Person> criteria = cb.createQuery(Person.class);
		Root<Person> person = criteria.from(Person.class);
		criteria.select(person).orderBy(cb.asc(person.get("personName")));
		return em.createQuery(criteria).getResultList();
	}

}
