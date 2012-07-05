package com.redhat.prototype.rest;

import com.redhat.prototype.data.PersonRepository;
import com.redhat.prototype.model.Person;
import com.redhat.prototype.service.PersonRegistration;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import javax.validation.Validator;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.logging.Logger;

@Path("/people")
@RequestScoped
public class PersonService {

    @Inject
    private Logger log;

    @Inject
    private Validator validator;

    @Inject
    private PersonRepository personRepository;

    @Inject
    PersonRegistration personRegistration;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Person> listAllPeople(@Context HttpServletRequest request) {
            log.info("Listed all people");
            return personRepository.findAllOrderedByName();
    }

    @GET
    @Path("/{id:[0-9][0-9]*}")
    @Produces(MediaType.APPLICATION_JSON)
    public Person lookupPersonById(@PathParam("id") long id) {

        Person person = personRepository.findById(id);

        if (person == null) {
            log.info("Could not find requested person with id: " + id);
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        log.info("Found person " + person.getPersonName() + " with id " + id);
        return person;
    }

    /**
     * Creates a new person from the values provided and generates an id/uri.
     * Performs validation, and will return a JAX-RS response with either 200
     * OK, or with a map of fields, and related errors.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createPerson(Person person) {

        Response.ResponseBuilder builder = null;

        if (person != null && person.getPersonId() != null) {
            String message = "Use PUT to update person with id "
                    + person.getPersonId();
            builder = Response.status(Response.Status.BAD_REQUEST).entity(
                    message);
        } else {
            try {
                // Validates member using bean validation
                validatePerson(person);
                personRegistration.register(person);
                log.info("Registered person " + person.getPersonName() + " with id: "
                        + person.getPersonId());
                // Create an "ok" response
                long id = person.getPersonId();
                String result = "Person created with id " + id + " at: "
                        + "/rest/people/" + id;
                builder = Response.ok(result);

            } catch (ConstraintViolationException ce) {
                // Handle bean validation issues
                builder = createViolationResponse(ce.getConstraintViolations());
            } catch (ValidationException e) {
                // Handle the unique constraint violations
                builder = createValidationResponse(e);
            } catch (Exception e) {
                // Handle generic exceptions
                builder = createGeneralExceptionResponse(e,
                        "Exception raised while creating person", e.getMessage());
            }
        }
        return builder.build();
    }

    /**
     * Creates or updates a person with the id specified. Performs validation,
     * and will return a JAX-RS response with either 200 OK, or with a map of
     * fields, and related errors.
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createOrUpdatePersonWithId(Person person) {

        Response.ResponseBuilder builder = null;

        if (person != null && person.getPersonId() == null) {
            String message = "Use POST to create a new person and generate an id";
            builder = Response.status(Response.Status.BAD_REQUEST).entity(
                    message);
        } else {
            try {
                // Validates member using bean validation
                validatePerson(person);

                Person personWithId = personRepository.findById(person.getPersonId());

                if (personWithId == null) {
                    log.info("Attempt to use PUT to create " + person.getPersonName()
                            + " with id: " + person.getPersonId());
                    String result = "No person to update at: "
                            + "/rest/people/" + person.getPersonId();
                    builder = Response.status(Response.Status.BAD_REQUEST)
                            .entity(result);

                } else {
                    personRegistration.update(person);
                    log.info("Updated person " + person.getPersonName()
                            + " with id: " + person.getPersonId());
                    // Create an "ok" response
                    String result = "Person at: " + "/rest/people/"
                            + person.getPersonId() + " is up-to-date";
                    builder = Response.ok(result);
                }
            } catch (ConstraintViolationException ce) {
                // Handle bean validation issues
                builder = createViolationResponse(ce.getConstraintViolations());
            } catch (ValidationException e) {
                // Handle the unique constraint violations
                builder = createValidationResponse(e);
            } catch (Exception e) {
                // Handle generic exceptions
                builder = createGeneralExceptionResponse(e,
                        "Exception raised while updating person", e.getMessage());
            }
        }
        return builder.build();
    }

    /**
     * Deletes the person with the id specified. Will return a 404 NOT FOUND if
     * there is no person with that URI.
     */

    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id:[0-9][0-9]*}")
    public Response deletePerson(@PathParam("id") long id) {

        Response.ResponseBuilder builder = null;

        Person person = personRepository.findById(id);

        if (person == null) {
            log.info("Could not find requested person with id: " + id);
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        } else {
            try {
                personRegistration.delete(person);
                log.info("Deleted person " + person.getPersonName() + " with id "
                        + id);
                builder = Response.ok("Deleted person with id " + id);
            } catch (Exception e) {
                builder = createGeneralExceptionResponse(e,
                        "Exception raised while deleting person", "Could not delete person");
            }
        }
        return builder.build();
    }

    /**
     * <p>
     * Validates the given Person variable and throws validation exceptions
     * based on the type of error. If the error is standard bean validation
     * errors then it will throw a ConstraintValidationException with the set of
     * the constraints violated.
     * </p>
     * <p>
     * If the error is caused because an existing person with the same email or
     * username is registered it throws a regular validation exception so that
     * it can be interpreted separately.
     * </p>
     *
     * @param person Person to be validated
     * @throws ConstraintViolationException If Bean Validation errors exist
     * @throws ValidationException          If member with the same email or username already exists
     */
    private void validatePerson(Person person)
            throws ConstraintViolationException, ValidationException {
        // Create a bean validator and check for issues.
        Set<ConstraintViolation<Person>> violations = validator
                .validate(person);

        if (!violations.isEmpty()) {
            log.info("Contraint violations was not empty");
            throw new ConstraintViolationException(
                    new HashSet<ConstraintViolation<?>>(violations));
        }

        // Check the uniqueness of the email address
        if (person.getPersonEmail() != null
                && emailAlreadyExists(person.getPersonEmail(), person.getPersonId())) {
            log.info("Email " + person.getPersonEmail() + " already exists");
            throw new ValidationException("Unique Email Violation");
        }

        // Check the uniqueness of the username
        if (person.getPersonUsername() != null
                && usernameAlreadyExists(person.getPersonUsername(), person.getPersonId())) {
            log.info("Username " + person.getPersonUsername() + "already exists");
            throw new ValidationException("Unique Username Violation");
        }
    }

    private Response.ResponseBuilder createValidationResponse(ValidationException e) {
        Response.ResponseBuilder builder;
        Map<String, String> responseObj = new HashMap<String, String>();
        if (e.getMessage().contains("Username")) {
            responseObj.put("username", "Username is taken");
        } else if (e.getMessage().contains("Email")) {
            responseObj.put("email", "Email is taken");
        } else {
            responseObj.put("id", "Details could not be validated");
        }
        builder = Response.status(Response.Status.CONFLICT).entity(
                responseObj);
        return builder;
    }

    private Response.ResponseBuilder createGeneralExceptionResponse(Exception e, String logMessage, String errorMessage) {
        Response.ResponseBuilder builder;
        log.warning(logMessage + ": "
                + e.getMessage());
        Map<String, String> responseObj = new HashMap<String, String>();
        responseObj.put("error", errorMessage);
        builder = Response.status(Response.Status.BAD_REQUEST).entity(
                responseObj);
        return builder;
    }

    /**
     * Creates a JAX-RS "Bad Request" response including a map of all violation
     * fields, and their message. This can then be used by clients to show
     * violations.
     *
     * @param violations A set of violations that needs to be reported
     * @return JAX-RS response containing all violations
     */
    private Response.ResponseBuilder createViolationResponse(
            Set<ConstraintViolation<?>> violations) {
        log.info("Validation completed. violations found: " + violations.size());

        Map<String, String> responseObj = new HashMap<String, String>();

        for (ConstraintViolation<?> violation : violations) {
            responseObj.put(violation.getPropertyPath().toString(),
                    violation.getMessage());
        }

        return Response.status(Response.Status.BAD_REQUEST).entity(responseObj);
    }

    /**
     * Checks if a person with the same email address is already registered.
     * This is the only way to easily capture the
     * "@UniqueConstraint(columnNames = "email")" constraint from the Person
     * class.
     *
     * @param email The email to check
     * @return True if the email already exists, and false otherwise
     */
    public boolean emailAlreadyExists(String email, Long id) {
        Person person = null;
        try {
            person = personRepository.findByEmail(email);
        } catch (NoResultException e) {
            // ignore
        }
        return person != null
                && detailDoesNotBelongToPersonBeingProcessed(id, person);
    }

    /**
     * Checks if a person with the same username is already registered. This is
     * the only way to easily capture the
     * "@UniqueConstraint(columnNames = "username")" constraint from the Person
     * class.
     *
     * @param username The username to check
     * @return True if the username already exists, and false otherwise
     */
    public boolean usernameAlreadyExists(String username, Long id) {
        Person person = null;
        try {
            person = personRepository.findByUsername(username);
        } catch (NoResultException e) {
            // ignore
        }
        return person != null
                && detailDoesNotBelongToPersonBeingProcessed(id, person);
    }

    private boolean detailDoesNotBelongToPersonBeingProcessed(Long id,
                                                              Person person) {
        return person.getPersonId() == null || person.getPersonId() != id;
    }
}