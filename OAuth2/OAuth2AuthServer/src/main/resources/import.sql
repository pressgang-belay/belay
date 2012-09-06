
insert into CLIENT (CLIENT_ID, CLIENT_IDENTIFIER, CLIENT_NAME, CLIENT_REDIRECT_URI) values (-1, 'OAuth2AuthServer', 'OAuth2AuthServer', '/auth/identity/completeAssociation')
insert into CLIENT (CLIENT_ID, CLIENT_IDENTIFIER, CLIENT_NAME, CLIENT_REDIRECT_URI) values (-2, 'pressgang_belay_id', 'GwtOAuth2Client', 'https://localhost:8443/OAuth2GwtClientApp/org.jboss.pressgang.belay.oauth2.gwt.sample.App/oAuthWindow.html')

insert into OPENID_PROVIDER (PROVIDER_ID, PROVIDER_NAME, PROVIDER_URL) values (-1, 'Google', 'gmail.com')
insert into OPENID_PROVIDER (PROVIDER_ID, PROVIDER_NAME, PROVIDER_URL) values (-2, 'Red Hat', 'https://localhost:8443/OpenIdProvider')
insert into OPENID_PROVIDER (PROVIDER_ID, PROVIDER_NAME, PROVIDER_URL) values (-3, 'Yahoo', 'yahoo.com')
insert into OPENID_PROVIDER (PROVIDER_ID, PROVIDER_NAME, PROVIDER_URL) values (-4, 'Facebook', 'facebook.com')
insert into OPENID_PROVIDER (PROVIDER_ID, PROVIDER_NAME, PROVIDER_URL) values (-5, 'myOpenID', 'myopenid.com')
insert into OPENID_PROVIDER (PROVIDER_ID, PROVIDER_NAME, PROVIDER_URL) values (-6, 'Fedora Account System', 'admin.fedoraproject.org')

insert into OPENID_IDENTITY (IDENTITY_ID, IDENTIFIER) values (-1, 'https://localhost:8443/OpenIdProvider/openid/provider?id=user')

insert into OPENID_USER (USER_ID, OPENID_IDENTITY_IDENTITY_ID) values (-1, -1)

insert into OPENID_IDENTITY_OPENID_USER (IDENTITY_ID, USER_ID) values (-1, -1)

insert into OPENID_IDENTITY_OPENID_PROVIDER (PROVIDER_ID, IDENTITY_ID) values (-2, -1)

insert into SCOPE (SCOPE_ID, SCOPE_NAME) values (-1, 'default')

insert into OPENID_IDENTITY_SCOPE (IDENTITY_ID, SCOPE_ID) values (-1, -1)



insert into RS_SCOPE (SCOPE_ID, SCOPE_NAME) values (-1, 'default')

insert into RS_ENDPOINT (ENDPOINT_ID, ENDPOINT_URL_PATTERN, ENDPOINT_METHOD) values (-1, 'https://localhost:8443/OAuth2AuthServer/rest/auth/identity/associate', 'GET')
insert into RS_ENDPOINT (ENDPOINT_ID, ENDPOINT_URL_PATTERN, ENDPOINT_METHOD) values (-2, 'https://localhost:8443/OAuth2AuthServer/rest/auth/identity/query', 'GET')
insert into RS_ENDPOINT (ENDPOINT_ID, ENDPOINT_URL_PATTERN, ENDPOINT_METHOD) values (-3, 'https://localhost:8443/OAuth2AuthServer/rest/auth/identity/makePrimary', 'GET')
insert into RS_ENDPOINT (ENDPOINT_ID, ENDPOINT_URL_PATTERN, ENDPOINT_METHOD) values (-4, 'https://localhost:8443/OAuth2AuthServer/rest/auth/identity/completeAssociation', 'GET')

insert into RS_SCOPE_RS_ENDPOINT (SCOPE_ID, ENDPOINT_ID) values (-1, -1)
insert into RS_SCOPE_RS_ENDPOINT (SCOPE_ID, ENDPOINT_ID) values (-1, -2)
insert into RS_SCOPE_RS_ENDPOINT (SCOPE_ID, ENDPOINT_ID) values (-1, -3)
insert into RS_SCOPE_RS_ENDPOINT (SCOPE_ID, ENDPOINT_ID) values (-1, -4)



