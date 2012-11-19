package org.jboss.pressgang.belay.oauth2.authserver.data.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.validator.constraints.Email;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Persistence logic for authenticated users.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
@Entity
@Table(name = "OPENID_IDENTITY")
public class Identity implements Serializable {

    private static final long serialVersionUID = -2816937391756095960L;

    private long identityId;
    private String identifier;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String language;
    private String country;
    private OpenIdProvider openIdProvider;
    private User user;

    public Identity() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IDENTITY_ID")
    public long getIdentityId() {
        return identityId;
    }

    @NotNull
    @Column(name = "IDENTIFIER", unique = true)
    public String getIdentifier() {
        return identifier;
    }

    @Column(name = "IDENTITY_FIRST_NAME")
    public String getFirstName() {
        return firstName;
    }

    @Column(name = "IDENTITY_LAST_NAME")
    public String getLastName() {
        return lastName;
    }

    @Column(name = "IDENTITY_FULL_NAME")
    public String getFullName() {
        return fullName;
    }

    @Email
    @Column(name = "IDENTITY_EMAIL")
    public String getEmail() {
        return email;
    }

    @Column(name = "IDENTITY_LANGUAGE")
    public String getLanguage() {
        return language;
    }

    @Column(name = "IDENTITY_COUNTRY")
    public String getCountry() {
        return country;
    }

    @NotNull
    @ManyToOne
    @JoinColumn(name = "PROVIDER_ID")
    public OpenIdProvider getOpenIdProvider() {
        return openIdProvider;
    }

    @NotNull
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH}, fetch = FetchType.EAGER)
    @JoinColumn(name = "USER_ID", referencedColumnName = "USER_ID")
    public User getUser() {
        return user;
    }

    public void setIdentityId(long identityId) {
        this.identityId = identityId;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setOpenIdProvider(OpenIdProvider openIdProvider) {
        this.openIdProvider = openIdProvider;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Identity)) return false;

        Identity that = (Identity) o;

        return new EqualsBuilder()
                .append(identifier, that.getIdentifier())
                .append(firstName, that.getFirstName())
                .append(lastName, that.getLastName())
                .append(fullName, that.getFullName())
                .append(email, that.getEmail())
                .append(country, that.getCountry())
                .append(language, that.getLanguage())
                .append(openIdProvider, that.getOpenIdProvider())
                .isEquals();
    }

    @Override
    public final int hashCode() {
        return new HashCodeBuilder()
                .append(identifier)
                .append(firstName)
                .append(lastName)
                .append(fullName)
                .append(email)
                .append(country)
                .append(language)
                .append(openIdProvider)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("identifier", identifier)
                .append("firstName", firstName)
                .append("lastName", lastName)
                .append("fullName", fullName)
                .append("email", email)
                .append("country", country)
                .append("language", language)
                .append("openIdProvider", openIdProvider)
                .append("user", user)
                .toString();
    }
}
