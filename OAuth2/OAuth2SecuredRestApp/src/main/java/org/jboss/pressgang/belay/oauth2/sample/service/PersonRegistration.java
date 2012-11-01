package org.jboss.pressgang.belay.oauth2.sample.service;

import org.jboss.pressgang.belay.oauth2.sample.model.Person;

import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.logging.Logger;

/**
 * Used to register people. Follows JBoss AS 7 Kitchensink example pattern.
 */
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
