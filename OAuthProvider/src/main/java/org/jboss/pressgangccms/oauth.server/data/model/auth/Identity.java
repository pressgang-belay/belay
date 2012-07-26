package org.jboss.pressgangccms.oauth.server.data.model.auth;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.validator.constraints.Email;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

/**
 * Persistence logic for authenticated users.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
@Entity
@Table(name="OPENID_IDENTITY", uniqueConstraints = @UniqueConstraint(columnNames = { "IDENTIFIER" }))
public class Identity implements Serializable {

    private static final long serialVersionUID = -2816937391756095960L;

    private BigInteger identityId;
    private String identifier;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String language;
    private String country;
    private OpenIdProvider openIdProvider;
    private User user;
    private Set<TokenGrant> tokenGrants = new HashSet<TokenGrant>();
    private Set<Scope> identityScopes = new HashSet<Scope>();

    public Identity() {
    }

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "IDENTITY_ID")
    public BigInteger getIdentityId() {
        return identityId;
    }

    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "IDENTIFIER")
    public String getIdentifier() {
        return identifier;
    }

    @Pattern(regexp = "[A-Za-z'-]*", message = "must contain only letters, hyphens or apostrophes")
    @Column(name = "IDENTITY_FIRST_NAME")
    public String getFirstName() {
        return firstName;
    }

    @Pattern(regexp = "[A-Za-z'-]*", message = "must contain only letters, hyphens or apostrophes")
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

    //@NotNull
    @ManyToOne
    @JoinTable(name="OPENID_IDENTITY_OPENID_PROVIDER", joinColumns = { @JoinColumn(name = "IDENTITY_ID") },
            inverseJoinColumns = { @JoinColumn(name = "PROVIDER_ID") })
    public OpenIdProvider getOpenIdProvider() {
        return openIdProvider;
    }

    //@NotNull
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinTable(name="OPENID_IDENTITY_OPENID_USER", joinColumns = { @JoinColumn(name = "IDENTITY_ID") },
            inverseJoinColumns = { @JoinColumn(name = "USER_ID") })
    public User getUser() {
        return user;
    }

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "grantIdentity")
    public Set<TokenGrant> getTokenGrants() {
        return tokenGrants;
    }

    @ManyToMany
    @JoinTable(name = "OPENID_IDENTITY_SCOPE", joinColumns = { @JoinColumn(name = "IDENTITY_ID") },
            inverseJoinColumns = { @JoinColumn(name = "SCOPE_ID") })
    public Set<Scope> getIdentityScopes() {
        return identityScopes;
    }

    public void setIdentityId(BigInteger identityId) {
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

    public void setTokenGrants(Set<TokenGrant> tokenGrants) {
        this.tokenGrants = tokenGrants;
    }

    public void setIdentityScopes(Set<Scope> identityScopes) {
        this.identityScopes = identityScopes;
    }

    @Override
    public boolean equals(Object o) {
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
    public int hashCode() {
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
        return new ToStringBuilder(this).append("identifier", identifier)
                .append("firstName", firstName)
                .append("lastName", lastName)
                .append("fullName", fullName)
                .append("email", email)
                .append("country", country)
                .append("language", language)
                .append("openIdProvider", openIdProvider)
                .append("user", user)
                .append("tokenGrants", tokenGrants)
                .append("identityScopes", identityScopes)
                .toString();
    }
}
