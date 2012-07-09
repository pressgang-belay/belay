package com.redhat.prototype.model.auth;

import org.hibernate.validator.constraints.Email;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@XmlRootElement
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
    private TokenGrant tokenGrant;
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

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "grantUser")
    public TokenGrant getTokenGrant() {
        return tokenGrant;
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

    public void setTokenGrant(TokenGrant tokenGrant) {
        this.tokenGrant = tokenGrant;
    }

    public void setUserScopes(Set<Scope> userScopes) {
        this.userScopes = userScopes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;

        User user = (User) o;

        if (openIdProvider != null ? !openIdProvider.equals(user.openIdProvider) : user.openIdProvider != null)
            return false;
        if (!userIdentifier.equals(user.userIdentifier)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = userIdentifier.hashCode();
        result = 31 * result + (openIdProvider != null ? openIdProvider.hashCode() : 0);
        return result;
    }
}
