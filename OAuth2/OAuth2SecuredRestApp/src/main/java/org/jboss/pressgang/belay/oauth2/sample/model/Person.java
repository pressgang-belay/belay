/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the 
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.pressgang.belay.oauth2.sample.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.math.BigInteger;

/**
 * Persistence logic for Person.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
@Entity
@XmlRootElement
@Table(name = "PERSON", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"PERSON_NAME"}),
        @UniqueConstraint(columnNames = {"PERSON_USERNAME"})
})
public class Person implements Serializable {

    private static final long serialVersionUID = 8570615351546278428L;

    private BigInteger personId;
    private String personName;
    private String personUsername;
    private String personEmail;

    Person() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PERSON_ID")
    public BigInteger getPersonId() {
        return personId;
    }

    @NotNull
    @Size(min = 1, max = 25)
    @Pattern(regexp = "[A-Za-z ]*", message = "must contain only letters and spaces")
    @Column(name = "PERSON_NAME")
    public String getPersonName() {
        return personName;
    }

    @NotNull
    @Size(min = 1, max = 15)
    @Pattern(regexp = "[A-Za-z]*", message = "must contain only letters")
    @Column(name = "PERSON_USERNAME")
    public String getPersonUsername() {
        return personUsername;
    }

    @NotNull
    @NotEmpty
    @Email
    @Column(name = "PERSON_EMAIL")
    public String getPersonEmail() {
        return personEmail;
    }

    public void setPersonId(BigInteger personId) {
        this.personId = personId;
    }

    public void setPersonName(String name) {
        this.personName = name;
    }

    public void setPersonUsername(String username) {
        this.personUsername = username;
    }

    public void setPersonEmail(String email) {
        this.personEmail = email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Person)) return false;

        Person that = (Person) o;

        return new EqualsBuilder()
                .append(personName, that.getPersonName())
                .append(personUsername, that.getPersonUsername())
                .append(personEmail, that.getPersonEmail())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(personName)
                .append(personUsername)
                .append(personEmail)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("personName", personName)
                .append("username", personUsername)
                .append("email", personEmail)
                .toString();
    }
}
