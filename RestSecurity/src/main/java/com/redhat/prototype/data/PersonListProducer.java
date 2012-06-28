package com.redhat.prototype.data;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.event.Reception;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;

import com.redhat.prototype.model.Person;

@RequestScoped
public class PersonListProducer {
	
	@Inject
	private PersonRepository personRepository;
	
	private List<Person> people;
	
	// @Named provides access the return value via the EL variable name "members" in the UI
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
