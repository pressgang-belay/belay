package com.redhat.prototype.service;

import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import com.redhat.prototype.model.Person;

//The @Stateless annotation eliminates the need for manual transaction demarcation
@Stateless
public class PersonRegistration {
	
	@Inject
	private Logger log;

	@Inject
	private EntityManager em;

	@Inject
	private Event<Person> personEventSrc;

	public void register(Person person) throws Exception {
		log.info("Registering " + person.getPersonName());
		em.persist(person);
		personEventSrc.fire(person);
	}
	
	public void update(Person person) throws Exception {
		log.info("Updating person " + person.getPersonId());
		em.merge(person);
		personEventSrc.fire(person);
	}
	
	public void delete(Person person) throws Exception {
		log.info("Deleting person " + person.getPersonId());
		em.remove(em.merge(person));
	}
}
