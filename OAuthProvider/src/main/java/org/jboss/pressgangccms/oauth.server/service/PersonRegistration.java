package org.jboss.pressgangccms.oauth.server.service;

import org.jboss.pressgangccms.oauth.server.data.model.Person;

import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.logging.Logger;

//The @Stateless annotation eliminates the need for manual transaction demarcation
@Stateless
public class PersonRegistration {
	
	@Inject
	private Logger log;

	@Inject
	private EntityManager em;

	@Inject
	private Event<Person> personEventSrc;

	public void register(Person person) {
		log.info("Registering " + person.getPersonName());
		em.persist(person);
		personEventSrc.fire(person);
	}
	
	public void update(Person person) {
		log.info("Updating person " + person.getPersonId());
		em.merge(person);
		personEventSrc.fire(person);
	}
	
	public void delete(Person person) {
		log.info("Deleting person " + person.getPersonId());
		em.remove(em.merge(person));
	}
}