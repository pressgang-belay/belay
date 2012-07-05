-- You can use this file to load seed data into the database using SQL statements
insert into PERSON (PERSON_ID, PERSON_NAME, PERSON_USERNAME, PERSON_EMAIL, PERSON_PASSWORD) values (0, 'John Smith', 'smithy', 'john.smith@mail.com', 'password')
insert into PERSON (PERSON_ID, PERSON_NAME, PERSON_USERNAME, PERSON_EMAIL, PERSON_PASSWORD) values (1, 'Jane Doe', 'jdoe', 'jane.doe@domail.com', 'Pa55w0rd')
insert into PERSON (PERSON_ID, PERSON_NAME, PERSON_USERNAME, PERSON_EMAIL, PERSON_PASSWORD) values (2, 'Clark Kent', 'cdawg', 'superdude99@flymail.com', 'CapeTown')

insert into CLIENT (CLIENT_ID, CLIENT_IDENTIFIER, CLIENT_NAME, CLIENT_SECRET) values (0, 'affbf16ab449cfa1e16392f705f9460', 'GWT Skynet', 'none')

insert into OPENID_USER (USER_ID, USER_IDENTIFIER) values (0, 'https://localhost:8443/OpenIdProvider/')

insert into SCOPE (SCOPE_ID, SCOPE_NAME) values (0, 'default')
