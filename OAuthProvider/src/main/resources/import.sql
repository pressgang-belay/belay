
insert into PERSON (PERSON_ID, PERSON_NAME, PERSON_USERNAME, PERSON_EMAIL, PERSON_PASSWORD) values (0, 'John Smith', 'smithy', 'john.smith@mail.com', 'password')
insert into PERSON (PERSON_ID, PERSON_NAME, PERSON_USERNAME, PERSON_EMAIL, PERSON_PASSWORD) values (1, 'Jane Doe', 'jdoe', 'jane.doe@domail.com', 'Pa55w0rd')
insert into PERSON (PERSON_ID, PERSON_NAME, PERSON_USERNAME, PERSON_EMAIL, PERSON_PASSWORD) values (2, 'Clark Kent', 'cdawg', 'superdude99@flymail.com', 'CapeTown')

insert into CLIENT (CLIENT_ID, CLIENT_IDENTIFIER, CLIENT_NAME, CLIENT_REDIRECT_URI, CLIENT_SECRET) values (-1, 'OAuthProvider', 'OAuthProvider', '/auth/user/associate', 'none')
insert into CLIENT (CLIENT_ID, CLIENT_IDENTIFIER, CLIENT_NAME, CLIENT_REDIRECT_URI, CLIENT_SECRET) values (-2, 'affbf16ab449cfa1e16392f705f9460', 'GwtOAuthClient', 'https://localhost:8443/GwtOAuthClient/org.jboss.pressgangccms.oauth.gwt.App/oAuthWindow.html', 'none')

insert into OPENID_PROVIDER (PROVIDER_ID, PROVIDER_NAME, PROVIDER_URL) values (-1, 'Google', 'gmail.com')
insert into OPENID_PROVIDER (PROVIDER_ID, PROVIDER_NAME, PROVIDER_URL) values (-2, 'Red Hat Kerberos', 'https://localhost:8443/OpenIdProvider/')

insert into OPENID_USER (USER_ID, USER_IDENTIFIER) values (-1, 'https://localhost:8443/OpenIdProvider/openid/provider?id=user')

insert into USER_GROUP (USER_GROUP_ID, OPENID_USER_USER_ID) values (-1, -1)

insert into USER_USER_GROUP (USER_ID, USER_GROUP_ID) values (-1, -1)

insert into USER_OPENID_PROVIDER (PROVIDER_ID, USER_ID) values (-2, -1)

insert into ENDPOINT (ENDPOINT_ID, ENDPOINT_URL_PATTERN, ENDPOINT_METHOD) values (-1, 'https://localhost:8443/OAuthProvider/rest/people', 'GET')
insert into ENDPOINT (ENDPOINT_ID, ENDPOINT_URL_PATTERN, ENDPOINT_METHOD) values (-2, 'https://localhost:8443/OAuthProvider/rest/people/[0-9]+', 'GET')
insert into ENDPOINT (ENDPOINT_ID, ENDPOINT_URL_PATTERN, ENDPOINT_METHOD) values (-3, 'https://localhost:8443/OAuthProvider/rest/people', 'POST')
insert into ENDPOINT (ENDPOINT_ID, ENDPOINT_URL_PATTERN, ENDPOINT_METHOD) values (-4, 'https://localhost:8443/OAuthProvider/rest/people', 'PUT')
insert into ENDPOINT (ENDPOINT_ID, ENDPOINT_URL_PATTERN, ENDPOINT_METHOD) values (-5, 'https://localhost:8443/OAuthProvider/rest/people/[0-9]+', 'DELETE')
insert into ENDPOINT (ENDPOINT_ID, ENDPOINT_URL_PATTERN, ENDPOINT_METHOD) values (-6, 'https://localhost:8443/OAuthProvider/rest/auth/user/associate', 'GET')

insert into SCOPE (SCOPE_ID, SCOPE_NAME) values (-1, 'default')

insert into SCOPE_ENDPOINT (SCOPE_ID, ENDPOINT_ID) values (-1, -1)
insert into SCOPE_ENDPOINT (SCOPE_ID, ENDPOINT_ID) values (-1, -2)
insert into SCOPE_ENDPOINT (SCOPE_ID, ENDPOINT_ID) values (-1, -6)

