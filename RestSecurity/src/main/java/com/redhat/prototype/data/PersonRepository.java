package com.redhat.prototype.data;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import com.redhat.prototype.model.Person;

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
		criteria.select(person).where(cb.equal(person.get("email"), email));
		return em.createQuery(criteria).getSingleResult();
	}
	
	public Person findByUsername(String username) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Person> criteria = cb.createQuery(Person.class);
		Root<Person> person = criteria.from(Person.class);
		// Swap criteria statements if you would like to try out type-safe
		// criteria queries, a new
		// feature in JPA 2.0
		// criteria.select(Person).where(cb.equal(Person.get(Person_.name),
		// email));
		criteria.select(person).where(cb.equal(person.get("username"), username));
		return em.createQuery(criteria).getSingleResult();
	}

	public List<Person> findAllOrderedByName() {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Person> criteria = cb.createQuery(Person.class);
		Root<Person> person = criteria.from(Person.class);
		// Swap criteria statements if you would like to try out type-safe
		// criteria queries, a new
		// feature in JPA 2.0
		// criteria.select(Person).orderBy(cb.asc(Person.get(Person_.name)));
		criteria.select(person).orderBy(cb.asc(person.get("name")));
		return em.createQuery(criteria).getResultList();
	}

}
