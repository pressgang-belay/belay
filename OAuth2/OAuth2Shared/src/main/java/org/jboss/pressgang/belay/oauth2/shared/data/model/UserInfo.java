package org.jboss.pressgang.belay.oauth2.shared.data.model;

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

    public void setPrimaryIdentifier(String primaryIdentifier) {
        this.primaryIdentifier = primaryIdentifier;
    }

    public void setUserIdentifiers(Set<String> userIdentifiers) {
        this.userIdentifiers = userIdentifiers;
    }

    public void setFirstNames(List<String> firstNames) {
        this.firstNames = firstNames;
    }

    public void setLastNames(List<String> lastNames) {
        this.lastNames = lastNames;
    }

    public void setFullNames(List<String> fullNames) {
        this.fullNames = fullNames;
    }

    public void setEmails(List<String> emails) {
        this.emails = emails;
    }

    public void setLanguages(List<String> languages) {
        this.languages = languages;
    }

    public void setCountries(List<String> countries) {
        this.countries = countries;
    }

    public void setOpenIdProviderUrls(List<String> openIdProviderUrls) {
        this.openIdProviderUrls = openIdProviderUrls;
    }

    public void setUserScopes(Set<String> userScopes) {
        this.userScopes = userScopes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserInfo)) return false;

        UserInfo that = (UserInfo) o;

        return primaryIdentifier.equals(that.getPrimaryIdentifier());
    }

    @Override
    public int hashCode() {
        return primaryIdentifier.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
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

        public static UserInfoBuilder identityInfoBuilder(String primaryIdentifier) {
            UserInfoBuilder builder = new UserInfoBuilder();
            builder.primaryIdentifier = primaryIdentifier;
            return builder;
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
            if (primaryIdentifier == null) {
                throw new IllegalArgumentException("Primary identifier cannot be null");
            }
            return new UserInfo(this);
        }
    }
}
