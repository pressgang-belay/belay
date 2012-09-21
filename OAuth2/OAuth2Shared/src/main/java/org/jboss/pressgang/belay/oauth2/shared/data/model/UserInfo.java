package org.jboss.pressgang.belay.oauth2.shared.data.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * Shared domain class encapsulating user information.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
@XmlRootElement
public class UserInfo implements Serializable {
    private static final long serialVersionUID = 5933390681273206069L;

    private String username;
    private String primaryIdentifier;
    private Set<String> userIdentifiers;
    private List<String> firstNames;
    private List<String> lastNames;
    private List<String> fullNames;
    private List<String> emails;
    private List<String> languages;
    private List<String> countries;
    private List<String> openIdProviderUrls;
    private Set<String> userScopes;

    UserInfo() {
    }

    private UserInfo(UserInfoBuilder builder) {
        this.username = builder.username;
        this.primaryIdentifier = builder.primaryIdentifier;
        this.userIdentifiers = builder.userIdentifiers;
        this.firstNames = builder.firstNames;
        this.lastNames = builder.lastNames;
        this.fullNames = builder.fullNames;
        this.emails = builder.emails;
        this.languages = builder.languages;
        this.countries = builder.countries;
        this.openIdProviderUrls = builder.openIdProviderUrls;
        this.userScopes = builder.userScopes;
    }

    public String getUsername() {
        return username;
    }

    public String getPrimaryIdentifier() {
        return primaryIdentifier;
    }

    public Set<String> getUserIdentifiers() {
        return userIdentifiers;
    }

    public List<String> getFirstNames() {
        return firstNames;
    }

    public List<String> getLastNames() {
        return lastNames;
    }

    public List<String> getFullNames() {
        return fullNames;
    }

    public List<String> getEmails() {
        return emails;
    }

    public List<String> getLanguages() {
        return languages;
    }

    public List<String> getCountries() {
        return countries;
    }

    public List<String> getOpenIdProviderUrls() {
        return openIdProviderUrls;
    }

    public Set<String> getUserScopes() {
        return userScopes;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserInfo)) return false;

        UserInfo that = (UserInfo) o;

        if (this.username == null || that.username == null) {
            return this.primaryIdentifier.equals(that.getPrimaryIdentifier());
        }
        if (this.primaryIdentifier == null || that.primaryIdentifier == null) {
            return this.username.equals(that.getUsername());
        }
        return new EqualsBuilder()
                .append(username, that.getUsername())
                .append(primaryIdentifier, that.getPrimaryIdentifier())
                .isEquals();
    }

    @Override
    public final int hashCode() {
        if (this.username == null) {
            return this.primaryIdentifier.hashCode();
        }
        if (this.primaryIdentifier == null) {
            return this.username.hashCode();
        }
        return new HashCodeBuilder()
                .append(username)
                .append(primaryIdentifier)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("username", username)
                .append("primaryIdentifier", primaryIdentifier)
                .append("userIdentifiers", userIdentifiers)
                .append("firstNames", firstNames)
                .append("lastNames", lastNames)
                .append("fullNames", fullNames)
                .append("emails", emails)
                .append("languages", languages)
                .append("countries", countries)
                .append("openIdProviderUrls", openIdProviderUrls)
                .append("userScopes", userScopes)
                .toString();
    }

    /**
     * Builder class to assist with creating UserInfo objects. Implements builder interface.
     */
    public static class UserInfoBuilder implements Builder<UserInfo> {
        private String username;
        private String primaryIdentifier;
        private Set<String> userIdentifiers;
        private List<String> firstNames;
        private List<String> lastNames;
        private List<String> fullNames;
        private List<String> emails;
        private List<String> languages;
        private List<String> countries;
        private List<String> openIdProviderUrls;
        private Set<String> userScopes;

        UserInfoBuilder() {
        }

        public static UserInfoBuilder identityInfoBuilder(String username) {
            UserInfoBuilder builder = new UserInfoBuilder();
            builder.username = username;
            return builder;
        }

        public UserInfoBuilder setPrimaryIdentifier(String primaryIdentifier) {
            this.primaryIdentifier = primaryIdentifier;
            return this;
        }

        public UserInfoBuilder setUserIdentifiers(Set<String> userIdentifiers) {
            this.userIdentifiers = userIdentifiers;
            return this;
        }

        public UserInfoBuilder setFirstNames(List<String> firstNames) {
            this.firstNames = firstNames;
            return this;
        }

        public UserInfoBuilder setLastNames(List<String> lastNames) {
            this.lastNames = lastNames;
            return this;
        }

        public UserInfoBuilder setFullNames(List<String> fullNames) {
            this.fullNames = fullNames;
            return this;
        }

        public UserInfoBuilder setEmails(List<String> emails) {
            this.emails = emails;
            return this;
        }

        public UserInfoBuilder setLanguages(List<String> languages) {
            this.languages = languages;
            return this;
        }

        public UserInfoBuilder setCountries(List<String> countries) {
            this.countries = countries;
            return this;
        }

        public UserInfoBuilder setOpenIdProviderUrls(List<String> openIdProviderUrls) {
            this.openIdProviderUrls = openIdProviderUrls;
            return this;
        }

        public UserInfoBuilder setUserScopes(Set<String> userScopes) {
            this.userScopes = userScopes;
            return this;
        }

        @Override
        public UserInfo build() {
            if (primaryIdentifier == null && username == null) {
                throw new java.lang.IllegalArgumentException("Primary identifier and username cannot both be null");
            }
            return new UserInfo(this);
        }
    }
}
