package org.jboss.pressgangccms.oauth.server.data.domain;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.jboss.pressgangccms.oauth.server.util.Builder;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Set;

/**
 * Non-persistent user domain class.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
@XmlRootElement
public class UserInfo implements Serializable {
    private static final long serialVersionUID = 5933390681273206069L;

    String userIdentifier;
    String firstName;
    String lastName;
    String email;
    String language;
    String country;
    String openIdProviderUrl;
    Boolean primaryUser;
    Set<String> userGroupIdentifiers;
    Set<String> userScopes;

    UserInfo() {
    }

    private UserInfo(UserInfoBuilder builder) {
        this.userIdentifier = builder.userIdentifier;
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
        this.email = builder.email;
        this.language = builder.language;
        this.country = builder.country;
        this.openIdProviderUrl = builder.openIdProviderUrl;
        this.primaryUser = builder.primaryUser;
        this.userGroupIdentifiers = builder.userGroupIdentifiers;
        this.userScopes = builder.userScopes;
    }

    public String getUserIdentifier() {
        return userIdentifier;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
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

    public Boolean getPrimaryUser() {
        return primaryUser;
    }

    public Set<String> getUserGroupIdentifiers() {
        return userGroupIdentifiers;
    }

    public Set<String> getUserScopes() {
        return userScopes;
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

    public void setOpenIdProviderUrl(String openIdProviderUrl) {
        this.openIdProviderUrl = openIdProviderUrl;
    }

    public void setPrimaryUser(Boolean primaryUser) {
        this.primaryUser = primaryUser;
    }

    public void setUserGroupIdentifiers(Set<String> userGroupIdentifiers) {
        this.userGroupIdentifiers = userGroupIdentifiers;
    }

    public void setUserScopes(Set<String> userScopes) {
        this.userScopes = userScopes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserInfo)) return false;

        UserInfo that = (UserInfo) o;

        return userIdentifier.equals(((UserInfo) o).getUserIdentifier());
    }

    @Override
    public int hashCode() {
        return userIdentifier.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("userIdentifier", userIdentifier)
                .append("firstName", firstName)
                .append("lastName", lastName)
                .append("email", email)
                .append("language", language)
                .append("country", country)
                .append("openIdProviderUrl", openIdProviderUrl)
                .append("isPrimaryUser", primaryUser)
                .append("userGroupIdentifiers", userGroupIdentifiers)
                .append("userScopes", userScopes)
                .toString();
    }

    public static class UserInfoBuilder implements Builder<UserInfo> {
        private String userIdentifier;
        private String firstName;
        private String lastName;
        private String email;
        private String language;
        private String country;
        private String openIdProviderUrl;
        private Boolean primaryUser;
        private Set<String> userGroupIdentifiers;
        private Set<String> userScopes;

        UserInfoBuilder() {
        }

        public static UserInfoBuilder userInfoBuilder(String userIdentifier) {
            UserInfoBuilder builder = new UserInfoBuilder();
            builder.userIdentifier = userIdentifier;
            return builder;
        }

        public UserInfoBuilder setFirstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public UserInfoBuilder setLastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public UserInfoBuilder setEmail(String email) {
            this.email = email;
            return this;
        }

        public UserInfoBuilder setLanguage(String language) {
            this.language = language;
            return this;
        }

        public UserInfoBuilder setCountry(String country) {
            this.country = country;
            return this;
        }

        public UserInfoBuilder setOpenIdProviderUrl(String openIdProviderUrl) {
            this.openIdProviderUrl = openIdProviderUrl;
            return this;
        }

        public UserInfoBuilder setPrimaryUser(Boolean isPrimaryUser) {
            this.primaryUser = isPrimaryUser;
            return this;
        }

        public UserInfoBuilder setUserGroupIdentifiers(Set<String> userGroupIdentifiers) {
            this.userGroupIdentifiers = userGroupIdentifiers;
            return this;
        }

        public UserInfoBuilder setUserScopes(Set<String> userScopes) {
            this.userScopes = userScopes;
            return this;
        }

        @Override
        public UserInfo build() {
            if (userIdentifier == null) {
                throw new IllegalArgumentException("User identifier cannot be null");
            }
            return new UserInfo(this);
        }
    }
}
