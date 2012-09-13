insert into RS_SCOPE (SCOPE_ID, SCOPE_NAME) values (-1, 'DEFAULT')

insert into RS_ENDPOINT (ENDPOINT_ID, ENDPOINT_URL_PATTERN, ENDPOINT_METHOD) values (-1, 'https://localhost:8443/OAuth2SecuredRestApp/rest/people', 'GET')
insert into RS_ENDPOINT (ENDPOINT_ID, ENDPOINT_URL_PATTERN, ENDPOINT_METHOD) values (-2, 'https://localhost:8443/OAuth2SecuredRestApp/rest/people/[0-9]+', 'GET')
insert into RS_ENDPOINT (ENDPOINT_ID, ENDPOINT_URL_PATTERN, ENDPOINT_METHOD) values (-3, 'https://localhost:8443/OAuth2SecuredRestApp/rest/people', 'POST')
insert into RS_ENDPOINT (ENDPOINT_ID, ENDPOINT_URL_PATTERN, ENDPOINT_METHOD) values (-4, 'https://localhost:8443/OAuth2SecuredRestApp/rest/people', 'PUT')
insert into RS_ENDPOINT (ENDPOINT_ID, ENDPOINT_URL_PATTERN, ENDPOINT_METHOD) values (-5, 'https://localhost:8443/OAuth2SecuredRestApp/rest/people/[0-9]+', 'DELETE')

insert into RS_SCOPE_RS_ENDPOINT (SCOPE_ID, ENDPOINT_ID) values (-1, -1)
insert into RS_SCOPE_RS_ENDPOINT (SCOPE_ID, ENDPOINT_ID) values (-1, -2)