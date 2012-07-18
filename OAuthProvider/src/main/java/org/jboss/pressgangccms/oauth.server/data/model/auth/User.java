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
import java.util.HashSet;
import java.util.Set;

/**
 * Persistence logic for authenticated users.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
@Entity
@Table(name="OPENID_USER", uniqueConstraints = @UniqueConstraint(columnNames = { "USER_IDENTIFIER" }))
public class User implements Serializable {

    private static final long serialVersionUID = -2816937391756095960L;

    private Long userId;
    private String userIdentifier;
    private String firstName;
    private String lastName;
    private String email;
    private String language;
    private String country;
    private OpenIdProvider openIdProvider;
    private Set<TokenGrant> tokenGrants = new HashSet<TokenGrant>();
    private Set<Scope> userScopes = new HashSet<Scope>();

    public User() {
    }

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "USER_ID")
    public Long getUserId() {
        return userId;
    }

    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "USER_IDENTIFIER")
    public String getUserIdentifier() {
        return userIdentifier;
    }

    @Pattern(regexp = "[A-Za-z'-]*", message = "must contain only letters, hyphens or apostrophes")
    @Column(name = "USER_FIRST_NAME")
    public String getFirstName() {
        return firstName;
    }

    @Pattern(regexp = "[A-Za-z'-]*", message = "must contain only letters, hyphens or apostrophes")
    @Column(name = "USER_LAST_NAME")
    public String getLastName() {
        return lastName;
    }

    @Email
    @Column(name = "USER_EMAIL")
    public String getEmail() {
        return email;
    }

    @Column(name = "USER_LANGUAGE")
    public String getLanguage() {
        return language;
    }

    @Column(name = "USER_COUNTRY")
    public String getCountry() {
        return country;
    }

    //@NotNull
    @ManyToOne
    @JoinTable(name="USER_OPENID_PROVIDER", joinColumns = { @JoinColumn(name = "USER_ID") },
            inverseJoinColumns = { @JoinColumn(name = "PROVIDER_ID") })
    public OpenIdProvider getOpenIdProvider() {
        return openIdProvider;
    }

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "grantUser")
    public Set<TokenGrant> getTokenGrants() {
        return tokenGrants;
    }

    @ManyToMany
    @JoinTable(name = "USER_SCOPE", joinColumns = { @JoinColumn(name = "USER_ID") },
            inverseJoinColumns = { @JoinColumn(name = "SCOPE_ID") })
    public Set<Scope> getUserScopes() {
        return userScopes;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setUserIdentifier(String userIdentifier) {
        this.userIdentifier = userIdentifier;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
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

    public void setTokenGrants(Set<TokenGrant> tokenGrants) {
        this.tokenGrants = tokenGrants;
    }

    public void setUserScopes(Set<Scope> userScopes) {
        this.userScopes = userScopes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;

        User that = (User) o;

        return new EqualsBuilder()
                .append(userIdentifier, that.getUserIdentifier())
                .append(firstName, that.getFirstName())
                .append(lastName, that.getLastName())
                .append(email, that.getEmail())
                .append(country, that.getCountry())
                .append(openIdProvider, that.getOpenIdProvider())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(userIdentifier)
                .append(firstName)
                .append(lastName)
                .append(email)
                .append(country)
                .append(openIdProvider)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("userIdentifier", userIdentifier)
                .append("firstName", firstName)
                .append("lastName", lastName)
                .append("email", email)
                .append("country", country)
                .append("openIdProvider", openIdProvider)
                .append("tokenGrants", tokenGrants)
                .append("userScopes", userScopes)
                .toString();
    }
}
