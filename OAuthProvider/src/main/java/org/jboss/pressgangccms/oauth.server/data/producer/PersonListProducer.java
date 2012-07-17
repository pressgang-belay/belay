package org.jboss.pressgangccms.oauth.server.data.producer;

import org.jboss.pressgangccms.oauth.server.data.dao.PersonRepository;
import org.jboss.pressgangccms.oauth.server.data.model.Person;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.event.Reception;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@RequestScoped
public class PersonListProducer {
	
	@Inject
	private PersonRepository personRepository;
	
	private List<Person> people;
	
	// @Named provides access to the returned value via the EL variable name "members" in the UI
	@Produces
	@Named
	public List<Person> getPeople() {
		return people;
	}
	
	public void onPersonListChanged(@Observes(notifyObserver = Reception.IF_EXISTS) final Person person) {
		retrieveAllPeopleOrderedByName();
	}

	@PostConstruct
	public void retrieveAllPeopleOrderedByName() {
		people = personRepository.findAllOrderedByName();
	}
}
