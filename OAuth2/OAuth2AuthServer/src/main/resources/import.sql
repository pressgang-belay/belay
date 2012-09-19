
insert into CLIENT (CLIENT_ID, CLIENT_IDENTIFIER, CLIENT_NAME, CLIENT_REDIRECT_URI, GRANTS_MUST_EXPIRE) values (-1, 'OAuth2AuthServer', 'OAuth2AuthServer', '/auth/user/associate/completeAssociation', true)
insert into CLIENT (CLIENT_ID, CLIENT_IDENTIFIER, CLIENT_NAME, CLIENT_REDIRECT_URI, GRANTS_MUST_EXPIRE) values (-2, 'pressgang_belay_id', 'GwtOAuth2Client', 'https://localhost:8443/OAuth2GwtClientApp/org.jboss.pressgang.belay.oauth2.gwt.sample.App/oAuthWindow.html', true)

insert into OPENID_PROVIDER (PROVIDER_ID, PROVIDER_NAME, PROVIDER_URL) values (-1, 'Google', 'gmail.com')
insert into OPENID_PROVIDER (PROVIDER_ID, PROVIDER_NAME, PROVIDER_URL) values (-2, 'Red Hat', 'https://localhost:8443/OpenIdProvider')
insert into OPENID_PROVIDER (PROVIDER_ID, PROVIDER_NAME, PROVIDER_URL) values (-3, 'Yahoo', 'me.yahoo.com')
insert into OPENID_PROVIDER (PROVIDER_ID, PROVIDER_NAME, PROVIDER_URL) values (-4, 'Facebook', 'facebook.com')
insert into OPENID_PROVIDER (PROVIDER_ID, PROVIDER_NAME, PROVIDER_URL) values (-5, 'myOpenID', 'myopenid.com')
insert into OPENID_PROVIDER (PROVIDER_ID, PROVIDER_NAME, PROVIDER_URL) values (-6, 'Fedora Account System', 'admin.fedoraproject.org')

insert into OPENID_IDENTITY (IDENTITY_ID, IDENTIFIER) values (-1, 'https://localhost:8443/OpenIdProvider/openid/provider?id=user')
-- insert into OPENID_IDENTITY (IDENTITY_ID, IDENTIFIER) values (-2, 'https://me.yahoo.com/a/1ZfO6j08s57hH1f0aHxpsHE9YoAqa1J1eKA-#8254d')
insert into OPENID_IDENTITY (IDENTITY_ID, IDENTIFIER) values (-3, 'https://www.google.com/accounts/o8/id?id=AItOawlu2J4UUb2RUlQnfbSZH2rdSZ0VQABj3I0')
insert into OPENID_IDENTITY (IDENTITY_ID, IDENTIFIER) values (-4, 'https://pressgangccms.myopenid.com/')
insert into OPENID_IDENTITY (IDENTITY_ID, IDENTIFIER) values (-5, 'https://admin.fedoraproject.org/accounts/openid/id/pressgangccms')

insert into OPENID_USER (USER_ID, OPENID_IDENTITY_IDENTITY_ID) values (-1, -1)
-- insert into OPENID_USER (USER_ID, OPENID_IDENTITY_IDENTITY_ID) values (-2, -2)
insert into OPENID_USER (USER_ID, OPENID_IDENTITY_IDENTITY_ID) values (-3, -3)
insert into OPENID_USER (USER_ID, OPENID_IDENTITY_IDENTITY_ID) values (-4, -4)
insert into OPENID_USER (USER_ID, OPENID_IDENTITY_IDENTITY_ID) values (-5, -5)

insert into OPENID_IDENTITY_OPENID_USER (IDENTITY_ID, USER_ID) values (-1, -1)
-- insert into OPENID_IDENTITY_OPENID_USER (IDENTITY_ID, USER_ID) values (-2, -2)
insert into OPENID_IDENTITY_OPENID_USER (IDENTITY_ID, USER_ID) values (-3, -3)
insert into OPENID_IDENTITY_OPENID_USER (IDENTITY_ID, USER_ID) values (-4, -4)
insert into OPENID_IDENTITY_OPENID_USER (IDENTITY_ID, USER_ID) values (-5, -5)

insert into OPENID_IDENTITY_OPENID_PROVIDER (PROVIDER_ID, IDENTITY_ID) values (-2, -1)
-- insert into OPENID_IDENTITY_OPENID_PROVIDER (PROVIDER_ID, IDENTITY_ID) values (-3, -2)
insert into OPENID_IDENTITY_OPENID_PROVIDER (PROVIDER_ID, IDENTITY_ID) values (-1, -3)
insert into OPENID_IDENTITY_OPENID_PROVIDER (PROVIDER_ID, IDENTITY_ID) values (-5, -4)
insert into OPENID_IDENTITY_OPENID_PROVIDER (PROVIDER_ID, IDENTITY_ID) values (-6, -5)

insert into SCOPE (SCOPE_ID, SCOPE_NAME) values (-1, 'DEFAULT')
insert into SCOPE (SCOPE_ID, SCOPE_NAME) values (-2, 'PERFORM_USER_MANAGEMENT')

insert into OPENID_USER_SCOPE (USER_ID, SCOPE_ID) values (-1, -1)
insert into OPENID_USER_SCOPE (USER_ID, SCOPE_ID) values (-1, -2)
-- insert into OPENID_USER_SCOPE (USER_ID, SCOPE_ID) values (-2, -1)
-- insert into OPENID_USER_SCOPE (USER_ID, SCOPE_ID) values (-2, -2)
insert into OPENID_USER_SCOPE (USER_ID, SCOPE_ID) values (-3, -1)
insert into OPENID_USER_SCOPE (USER_ID, SCOPE_ID) values (-3, -2)
insert into OPENID_USER_SCOPE (USER_ID, SCOPE_ID) values (-4, -1)
insert into OPENID_USER_SCOPE (USER_ID, SCOPE_ID) values (-4, -2)
insert into OPENID_USER_SCOPE (USER_ID, SCOPE_ID) values (-5, -1)
insert into OPENID_USER_SCOPE (USER_ID, SCOPE_ID) values (-5, -2)


insert into RS_SCOPE (SCOPE_ID, SCOPE_NAME) values (-1, 'DEFAULT')
insert into RS_SCOPE (SCOPE_ID, SCOPE_NAME) values (-2, 'PERFORM_USER_MANAGEMENT')

insert into RS_ENDPOINT (ENDPOINT_ID, ENDPOINT_URL_PATTERN, ENDPOINT_METHOD) values (-1, 'https://localhost:8443/OAuth2AuthServer/rest/auth/user/associate/associateIdentity', 'GET')
insert into RS_ENDPOINT (ENDPOINT_ID, ENDPOINT_URL_PATTERN, ENDPOINT_METHOD) values (-2, 'https://localhost:8443/OAuth2AuthServer/rest/auth/user/associate/completeAssociation', 'GET')
insert into RS_ENDPOINT (ENDPOINT_ID, ENDPOINT_URL_PATTERN, ENDPOINT_METHOD) values (-3, 'https://localhost:8443/OAuth2AuthServer/rest/auth/user/makeIdentityPrimary', 'GET')
insert into RS_ENDPOINT (ENDPOINT_ID, ENDPOINT_URL_PATTERN, ENDPOINT_METHOD) values (-4, 'https://localhost:8443/OAuth2AuthServer/rest/auth/user/queryIdentity', 'GET')
insert into RS_ENDPOINT (ENDPOINT_ID, ENDPOINT_URL_PATTERN, ENDPOINT_METHOD) values (-5, 'https://localhost:8443/OAuth2AuthServer/rest/auth/user/queryUser', 'GET')

insert into RS_SCOPE_RS_ENDPOINT (SCOPE_ID, ENDPOINT_ID) values (-2, -1)
insert into RS_SCOPE_RS_ENDPOINT (SCOPE_ID, ENDPOINT_ID) values (-2, -2)
insert into RS_SCOPE_RS_ENDPOINT (SCOPE_ID, ENDPOINT_ID) values (-2, -3)
insert into RS_SCOPE_RS_ENDPOINT (SCOPE_ID, ENDPOINT_ID) values (-2, -4)
insert into RS_SCOPE_RS_ENDPOINT (SCOPE_ID, ENDPOINT_ID) values (-2, -5)



