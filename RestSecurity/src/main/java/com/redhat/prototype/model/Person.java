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
package com.redhat.prototype.model;

import java.io.Serializable;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

@Entity
@XmlRootElement
@Table(name="PERSON", uniqueConstraints = @UniqueConstraint(columnNames = { "PERSON_NAME",
		"PERSON_USERNAME" }))
public class Person implements Serializable {

	private static final long serialVersionUID = 8570615351546278428L;

	private Long personId;
	private String personName;
	private String personUsername;
	private String personEmail;
	private String password;

    protected Person() {
    }

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "PERSON_ID")
	public Long getPersonId() {
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

    @NotNull
    @Size(min = 1, max = 15)
    @Pattern(regexp = "[A-Za-z0-9!_]*", message = "must contain only letters, numbers or the characters ! or _")
    @Column(name = "PERSON_PASSWORD")
    public String getPersonPassword() {
        return password;
    }

    public void setPersonId(Long personId) {
        this.personId = personId;
    }

    public void setPersonName(String name) {
        this.personName = name;
    }

    public void setPersonUsername(String personUsername) {
        this.personUsername = personUsername;
    }

	public void setPersonEmail(String personEmail) {
		this.personEmail = personEmail;
	}

	public void setPersonPassword(String password) {
		this.password = password;
	}
}
