
insert into PERSON (PERSON_ID, PERSON_NAME, PERSON_USERNAME, PERSON_EMAIL, PERSON_PASSWORD) values (0, 'John Smith', 'smithy', 'john.smith@mail.com', 'password')
insert into PERSON (PERSON_ID, PERSON_NAME, PERSON_USERNAME, PERSON_EMAIL, PERSON_PASSWORD) values (1, 'Jane Doe', 'jdoe', 'jane.doe@domail.com', 'Pa55w0rd')
insert into PERSON (PERSON_ID, PERSON_NAME, PERSON_USERNAME, PERSON_EMAIL, PERSON_PASSWORD) values (2, 'Clark Kent', 'cdawg', 'superdude99@flymail.com', 'CapeTown')

insert into CLIENT (CLIENT_ID, CLIENT_IDENTIFIER, CLIENT_NAME, CLIENT_REDIRECT_URI, CLIENT_SECRET) values (0, 'affbf16ab449cfa1e16392f705f9460', 'GWT Skynet', 'https://localhost:8443/GwtSecurity/com.redhat.gwtsecurity.App/oAuthWindow.html', 'none')

insert into OPENID_PROVIDER (PROVIDER_ID, PROVIDER_NAME, PROVIDER_URL) values (0, 'Google', 'gmail.com')
insert into OPENID_PROVIDER (PROVIDER_ID, PROVIDER_NAME, PROVIDER_URL) values (1, 'Red Hat Kerberos', 'https://localhost:8443/OpenIdProvider/')

insert into OPENID_USER (USER_ID, USER_IDENTIFIER) values (0, 'https://localhost:8443/OpenIdProvider/')

insert into USER_OPENID_PROVIDER (PROVIDER_ID, USER_ID) values (1, 0)

insert into SCOPE (SCOPE_ID, SCOPE_NAME) values (0, 'default')

