package com.redhat.prototype.model.auth;

import org.hibernate.validator.constraints.Email;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Set;

@Entity
@XmlRootElement
@Table(name="OPENID_USER", uniqueConstraints = @UniqueConstraint(columnNames = { "USER_IDENTIFIER" }))
public class User implements Serializable {

    private static final long serialVersionUID = -2816937391756095960L;

    private Long userId;
    private String identifier;
    private String firstName;
    private String lastName;
    private String email;
    private String language;
    private String country;
    private Set<TokenGrant> tokenGrants;
    private Set<Scope> userScopes;

    protected User() {
    }

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "USER_ID")
    public Long getUserId() {
        return userId;
    }

    @NotNull
    @Size(min = 1, max = 100)
    @Pattern(regexp = "[A-Za-z0-9!_@.]*", message = "must contain only letters, numbers or the characters: [!_@.]")
    @Column(name = "USER_IDENTIFIER")
    public String getIdentifier() {
        return identifier;
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

    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(name="USER_TOKEN_GRANT", joinColumns = { @JoinColumn(name = "USER_ID") },
            inverseJoinColumns = { @JoinColumn(name = "TOKEN_GRANT_ID") })
    public Set<TokenGrant> getTokenGrants() {
        return tokenGrants;
    }

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "USER_SCOPE", joinColumns = { @JoinColumn(name = "USER_ID") }, inverseJoinColumns = { @JoinColumn(name = "SCOPE_ID") })
    public Set<Scope> getUserScopes() {
        return userScopes;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

    public void setEmail(String email) {
        this.email = email;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setTokenGrants(Set<TokenGrant> tokenGrants) {
        this.tokenGrants = tokenGrants;
    }

    public void setUserScopes(Set<Scope> userScopes) {
        this.userScopes = userScopes;
    }
}
