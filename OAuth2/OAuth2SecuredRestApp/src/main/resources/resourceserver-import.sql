insert into RS_SCOPE (SCOPE_ID, SCOPE_NAME) values (-1, 'DEFAULT')

insert into RS_ENDPOINT (ENDPOINT_ID, ENDPOINT_URL, ENDPOINT_METHOD, URL_REGEX) values (-1, 'https://localhost:8443/OAuth2SecuredRestApp/rest/people', 'GET', false)
insert into RS_ENDPOINT (ENDPOINT_ID, ENDPOINT_URL, ENDPOINT_METHOD, URL_REGEX) values (-2, 'https://localhost:8443/OAuth2SecuredRestApp/rest/people/[0-9]+', 'GET', true)
insert into RS_ENDPOINT (ENDPOINT_ID, ENDPOINT_URL, ENDPOINT_METHOD, URL_REGEX) values (-3, 'https://localhost:8443/OAuth2SecuredRestApp/rest/people', 'POST', false)
insert into RS_ENDPOINT (ENDPOINT_ID, ENDPOINT_URL, ENDPOINT_METHOD, URL_REGEX) values (-4, 'https://localhost:8443/OAuth2SecuredRestApp/rest/people', 'PUT', false)
insert into RS_ENDPOINT (ENDPOINT_ID, ENDPOINT_URL, ENDPOINT_METHOD, URL_REGEX) values (-5, 'https://localhost:8443/OAuth2SecuredRestApp/rest/people/[0-9]+', 'DELETE', true)

insert into RS_SCOPE_RS_ENDPOINT (SCOPE_ID, ENDPOINT_ID) values (-1, -1)
insert into RS_SCOPE_RS_ENDPOINT (SCOPE_ID, ENDPOINT_ID) values (-1, -2)