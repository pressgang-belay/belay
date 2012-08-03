package org.jboss.pressgangccms.oauth2.sample.data;

import org.jboss.pressgangccms.oauth2.sample.model.Person;

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
