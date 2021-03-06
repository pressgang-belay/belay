package org.jboss.pressgang.belay.oauth2.shared.data.model;

import org.apache.commons.lang.builder.ToStringBuilder;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * Shared domain class encapsulating identity information.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
@XmlRootElement
public class IdentityInfo implements Serializable {
    private static final long serialVersionUID = 5933390681273206069L;

    private String identifier;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String language;
    private String country;
    private String openIdProviderUrl;
    private Boolean primaryIdentity;

    IdentityInfo() {
    }

    private IdentityInfo(IdentityInfoBuilder builder) {
        this.identifier = builder.identifier;
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
        this.fullName = builder.fullName;
        this.email = builder.email;
        this.language = builder.language;
        this.country = builder.country;
        this.openIdProviderUrl = builder.openIdProviderUrl;
        this.primaryIdentity = builder.primaryIdentity;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getLanguage() {
        return language;
    }

    public String getCountry() {
        return country;
    }

    public String getOpenIdProviderUrl() {
        return openIdProviderUrl;
    }

    public Boolean getPrimaryIdentity() {
        return primaryIdentity;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IdentityInfo)) return false;

        IdentityInfo that = (IdentityInfo) o;

        return identifier.equals(that.getIdentifier());
    }

    @Override
    public final int hashCode() {
        return identifier.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("identifier", identifier)
                .append("firstName", firstName)
                .append("lastName", lastName)
                .append("fullName", fullName)
                .append("email", email)
                .append("language", language)
                .append("country", country)
                .append("openIdProviderUrl", openIdProviderUrl)
                .append("isPrimaryIdentity", primaryIdentity)
                .toString();
    }

    /**
     * Builder class to assist with creating IdentityInfo objects. Implements builder interface.
     */
    public static class IdentityInfoBuilder implements Builder<IdentityInfo> {
        private String identifier;
        private String firstName;
        private String lastName;
        private String fullName;
        private String email;
        private String language;
        private String country;
        private String openIdProviderUrl;
        private Boolean primaryIdentity;

        IdentityInfoBuilder() {
        }

        public static IdentityInfoBuilder identityInfoBuilder(String userIdentifier) {
            IdentityInfoBuilder builder = new IdentityInfoBuilder();
            builder.identifier = userIdentifier;
            return builder;
        }

        public IdentityInfoBuilder setFirstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public IdentityInfoBuilder setLastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public IdentityInfoBuilder setFullName(String fullName) {
            this.fullName = fullName;
            return this;
        }

        public IdentityInfoBuilder setEmail(String email) {
            this.email = email;
            return this;
        }

        public IdentityInfoBuilder setLanguage(String language) {
            this.language = language;
            return this;
        }

        public IdentityInfoBuilder setCountry(String country) {
            this.country = country;
            return this;
        }

        public IdentityInfoBuilder setOpenIdProviderUrl(String openIdProviderUrl) {
            this.openIdProviderUrl = openIdProviderUrl;
            return this;
        }

        public IdentityInfoBuilder setPrimaryIdentity(boolean isPrimaryUser) {
            this.primaryIdentity = isPrimaryUser;
            return this;
        }

        @Override
        public IdentityInfo build() {
            if (identifier == null || primaryIdentity == null || openIdProviderUrl == null) {
                throw new java.lang.IllegalArgumentException("Required UserInfo attribute set to null");
            }
            return new IdentityInfo(this);
        }
    }
}
