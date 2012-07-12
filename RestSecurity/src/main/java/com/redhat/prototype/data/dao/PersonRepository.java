package com.redhat.prototype.data.dao;

import com.google.appengine.repackaged.com.google.common.base.Optional;
import com.redhat.prototype.data.model.Person;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

@ApplicationScoped
public class PersonRepository {

    @Inject
	private EntityManager em;

	public Optional<Person> findById(Long id) {
		Person person = em.find(Person.class, id);
        if (person == null) {
            return Optional.absent();
        }
        return Optional.of(person);
	}

	public Optional<Person> findByEmail(String email) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Person> criteria = cb.createQuery(Person.class);
		Root<Person> person = criteria.from(Person.class);
		// Swap criteria statements if you would like to try out type-safe
		// criteria queries, a new
		// feature in JPA 2.0
		// criteria.select(Person).where(cb.equal(Person.get(Person_.name),
		// email));
		criteria.select(person).where(cb.equal(person.get("personEmail"), email));
        TypedQuery<Person> query = em.createQuery(criteria);
        if (query.getResultList().size() == 1) {
            return Optional.of(query.getSingleResult());
        } else {
            return Optional.absent();
        }
	}
	
	public Optional<Person> findByUsername(String username) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Person> criteria = cb.createQuery(Person.class);
		Root<Person> person = criteria.from(Person.class);
		criteria.select(person).where(cb.equal(person.get("personUsername"), username));
        TypedQuery<Person> query = em.createQuery(criteria);
        if (query.getResultList().size() == 1) {
            return Optional.of(query.getSingleResult());
        } else {
            return Optional.absent();
        }
	}

	public List<Person> findAllOrderedByName() {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Person> criteria = cb.createQuery(Person.class);
		Root<Person> person = criteria.from(Person.class);
		criteria.select(person).orderBy(cb.asc(person.get("personName")));
		return em.createQuery(criteria).getResultList();
	}
}
