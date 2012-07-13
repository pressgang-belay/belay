package com.redhat.prototype.data.dao;

import com.google.appengine.repackaged.com.google.common.base.Optional;
import com.redhat.prototype.data.model.Person;
import com.redhat.prototype.data.model.Person_;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.logging.Logger;

@ApplicationScoped
public class PersonRepository {

    @Inject
    private EntityManager em;

    @Inject
    private Logger log;

	public Optional<Person> findById(Long id) {
		Person person = em.find(Person.class, id);
        if (person == null) {
            log.fine("Could not find Person with id " + id);
            return Optional.absent();
        }
        log.fine("Returning Person with id " + id);
        return Optional.of(person);
	}

	public Optional<Person> findByEmail(String email) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Person> criteria = cb.createQuery(Person.class);
		Root<Person> person = criteria.from(Person.class);
		criteria.select(person).where(cb.equal(person.get(Person_.personEmail), email));
        TypedQuery<Person> query = em.createQuery(criteria);
        if (query.getResultList().size() == 1) {
            log.fine("Returning Person with email " + email);
            return Optional.of(query.getSingleResult());
        } else {
            log.fine("Could not find Person with email " + email);
            return Optional.absent();
        }
	}
	
	public Optional<Person> findByUsername(String username) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Person> criteria = cb.createQuery(Person.class);
		Root<Person> person = criteria.from(Person.class);
		criteria.select(person).where(cb.equal(person.get(Person_.personUsername), username));
        TypedQuery<Person> query = em.createQuery(criteria);
        if (query.getResultList().size() == 1) {
            log.fine("Returning Person with username " + username);
            return Optional.of(query.getSingleResult());
        } else {
            log.fine("Could not find Person with username " + username);
            return Optional.absent();
        }
	}

	public List<Person> findAllOrderedByName() {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Person> criteria = cb.createQuery(Person.class);
		Root<Person> person = criteria.from(Person.class);
		criteria.select(person).orderBy(cb.asc(person.get(Person_.personName)));
        List<Person> resultList = em.createQuery(criteria).getResultList();
        log.fine("Returning list of " + resultList.size()  + " Person objects ordered by name");
        return resultList;
	}
}
