package org.jboss.pressgangccms.oauth.gwt.client;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import org.jboss.pressgangccms.oauth.gwt.client.oauth.AuthorisationRequest;
import org.jboss.pressgangccms.oauth.gwt.client.oauth.Authoriser;
import org.jboss.pressgangccms.oauth.gwt.client.oauth.OAuthHandler;
import org.jboss.pressgangccms.oauth.gwt.client.oauth.OAuthRequest;

public class App implements EntryPoint {

    private static final OAuthHandler AUTH_HANDLER = OAuthHandler.get();
    private static final String PROTOCOL_STRING = "https://";
    private static final String GOOGLE_PROVIDER_URL = "gmail.com";
    private static final String YAHOO_PROVIDER_URL = "yahoo.com";
    private static final String FACEBOOK_PROVIDER_URL = "www.facebook.com";
    private static final String MYOPENID_PROVIDER_URL = ".myopenid.com";
    private static final String MYID_PROVIDER_URL = ".myid.net";
    private static final String RED_HAT_PROVIDER_URL = "https://localhost:8443/OpenIdProvider/";
    private static final String PEOPLE_URL = "https://localhost:8443/OAuthProvider/rest/people";
    private static final String PERSON_1_URL = "https://localhost:8443/OAuthProvider/rest/people/1";
    private static final String SKYNET_USER_QUERY_URL = "https://localhost:8443/OAuthProvider/rest/auth/identity/query";
    private static final String SKYNET_ASSOCIATE_URL = "https://localhost:8443/OAuthProvider/rest/auth/identity/associate";
    private static final String SKYNET_MAKE_PRIMARY_URL = "https://localhost:8443/OAuthProvider/rest/auth/identity/makePrimary";
    private static final String SKYNET_LOGIN_URL = "https://localhost:8443/OAuthProvider/rest/auth/login";
    private static final String SKYNET_TOKEN_URL = "https://localhost:8443/OAuthProvider/rest/auth/token";
    private static final String SKYNET_CLIENT_SECRET = "none";
    private static final String SKYNET_CLIENT_ID = "affbf16ab449cfa1e16392f705f9460";
    private final String GOOGLE_USER_ID = "https://www.google.com/accounts/o8/id?id=AItOawmOODmBoSGeBzdngbGS1ltF0Caegz6ajVE";
    private final String PROVIDER_PARAM_STRING = "?provider=";
    private final String USER_ID_PARAM_STRING = "?id=";
    private final String TOKEN_PARAM_STRING = "&oauth_token=";
    private static String currentToken;

    public void onModuleLoad() {
        Authoriser.export();
        addRedHatLogin();
        addGoogleLogin();
        addYahooLogin();
        addFacebookLogin();
        addMyOpenIdLogin();
        addMyIdLogin();
        addGetPeople();
        addGetPerson();
        addAssociateIdentity();
        makeGoogleIdentityPrimary();
        getIdentityInfo();
        addClearTokens();
        addRefresh();
    }

    private void addRedHatLogin() {
        // Since the auth flow requires opening a popup window, it must be started
        // as a direct result of a user action, such as clicking a button or link.
        // Otherwise, a browser's popup blocker may block the popup.
        Button button = new Button("Login with Red Hat");
        button.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final AuthorisationRequest request = new AuthorisationRequest(SKYNET_LOGIN_URL + PROVIDER_PARAM_STRING
                        + RED_HAT_PROVIDER_URL, SKYNET_TOKEN_URL,
                        SKYNET_CLIENT_ID, SKYNET_CLIENT_SECRET);
                AUTH_HANDLER.login(request, getStandardCallback());
            }
        });
        RootPanel.get().add(button);
    }

    private void addGoogleLogin() {
        Button button = new Button("Login with Google");
        button.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final AuthorisationRequest request = new AuthorisationRequest(SKYNET_LOGIN_URL + PROVIDER_PARAM_STRING
                        + GOOGLE_PROVIDER_URL, SKYNET_TOKEN_URL,
                        SKYNET_CLIENT_ID, SKYNET_CLIENT_SECRET);
                AUTH_HANDLER.login(request, getStandardCallback());
            }
        });
        RootPanel.get().add(button);
    }

    private void addYahooLogin() {
        Button button = new Button("Login with Yahoo");
        button.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final AuthorisationRequest request = new AuthorisationRequest(SKYNET_LOGIN_URL + PROVIDER_PARAM_STRING
                        + YAHOO_PROVIDER_URL, SKYNET_TOKEN_URL,
                        SKYNET_CLIENT_ID, SKYNET_CLIENT_SECRET);
                AUTH_HANDLER.login(request, getStandardCallback());
            }
        });
        RootPanel.get().add(button);
    }

    private void addFacebookLogin() {
        Button button = new Button("Login with Facebook");
        button.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final AuthorisationRequest request = new AuthorisationRequest(SKYNET_LOGIN_URL + PROVIDER_PARAM_STRING
                        + FACEBOOK_PROVIDER_URL, SKYNET_TOKEN_URL,
                        SKYNET_CLIENT_ID, SKYNET_CLIENT_SECRET);
                AUTH_HANDLER.login(request, getStandardCallback());
            }
        });
        RootPanel.get().add(button);
    }

    private void addMyOpenIdLogin() {
        Button button = new Button("Login with MyOpenID.com");
        Label idLabel = new Label("MyOpenID identifier: ");
        final TextBox idTextBox = new TextBox();
        button.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final AuthorisationRequest request = new AuthorisationRequest(SKYNET_LOGIN_URL + PROVIDER_PARAM_STRING
                        + PROTOCOL_STRING + idTextBox.getText() + MYOPENID_PROVIDER_URL, SKYNET_TOKEN_URL,
                        SKYNET_CLIENT_ID, SKYNET_CLIENT_SECRET);
                AUTH_HANDLER.login(request, getStandardCallback());
            }
        });
        RootPanel.get().add(idLabel);
        RootPanel.get().add(idTextBox);
        RootPanel.get().add(button);
    }

    private void addMyIdLogin() {
        Button button = new Button("Login with MyID.net");
        Label idLabel = new Label("MyID.net identifier: ");
        final TextBox idTextBox = new TextBox();
        button.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final AuthorisationRequest request = new AuthorisationRequest(SKYNET_LOGIN_URL + PROVIDER_PARAM_STRING
                        + PROTOCOL_STRING + idTextBox.getText() + MYID_PROVIDER_URL, SKYNET_TOKEN_URL,
                        SKYNET_CLIENT_ID, SKYNET_CLIENT_SECRET);
                AUTH_HANDLER.login(request, getStandardCallback());
            }
        });
        RootPanel.get().add(idLabel);
        RootPanel.get().add(idTextBox);
        RootPanel.get().add(button);
    }

    private void addGetPeople() {
        Button button = new Button("GET all people");
        button.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                AUTH_HANDLER.sendRequest(new OAuthRequest(RequestBuilder.GET, PEOPLE_URL), getStandardRequestCallback());
            }
        });
        RootPanel.get().add(button);
    }

    private void addGetPerson() {
        Button button = new Button("GET person one");
        button.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                AUTH_HANDLER.sendRequest(new OAuthRequest(RequestBuilder.GET, PERSON_1_URL), getStandardRequestCallback());
            }
        });
        RootPanel.get().add(button);
    }

    private void addAssociateIdentity() {
        Label providerLabel = new Label("OpenID provider URL: ");
        final TextBox providerTextBox = new TextBox();
        Button button = new Button("Associate provider identity");

        button.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final AuthorisationRequest request = new AuthorisationRequest(SKYNET_ASSOCIATE_URL + PROVIDER_PARAM_STRING
                        + providerTextBox.getText() + TOKEN_PARAM_STRING + currentToken, SKYNET_TOKEN_URL,
                        SKYNET_CLIENT_ID, SKYNET_CLIENT_SECRET).forceNewRequest(true);
                AUTH_HANDLER.login(request, getStandardCallback());
            }
        });
        RootPanel.get().add(providerLabel);
        RootPanel.get().add(providerTextBox);
        RootPanel.get().add(button);
    }

    private void makeGoogleIdentityPrimary() {
        Button button = new Button("Make Google identity primary");
        button.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final AuthorisationRequest request = new AuthorisationRequest(SKYNET_MAKE_PRIMARY_URL
                        + USER_ID_PARAM_STRING + AUTH_HANDLER.encodeUrl(GOOGLE_USER_ID) + TOKEN_PARAM_STRING + currentToken,
                        SKYNET_TOKEN_URL, SKYNET_CLIENT_ID, SKYNET_CLIENT_SECRET).forceNewRequest(true);
                AUTH_HANDLER.login(request, getStandardCallback());
            }
        });
        RootPanel.get().add(button);
    }

    private void getIdentityInfo() {
        Button button = new Button("GET identity info");
        button.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                AUTH_HANDLER.sendRequest(new OAuthRequest(RequestBuilder.GET, SKYNET_USER_QUERY_URL), getStandardRequestCallback());
            }
        });
        RootPanel.get().add(button);
    }

    private void addRefresh() {
        Button button = new Button("Force token refresh");
        button.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final AuthorisationRequest request = new AuthorisationRequest(SKYNET_LOGIN_URL + PROVIDER_PARAM_STRING
                        + RED_HAT_PROVIDER_URL, SKYNET_TOKEN_URL,
                        SKYNET_CLIENT_ID, SKYNET_CLIENT_SECRET);
                AUTH_HANDLER.doRefresh(request, getStandardCallback());
            }
        });
        RootPanel.get().add(button);
    }

    // Clears all tokens stored in the browser by this library. Subsequent calls
    // to authorise() will result in the popup being shown, though it may immediately
    // disappear if the token has not expired.
    private void addClearTokens() {
        Button button = new Button("Clear stored tokens");
        button.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                OAuthHandler.get().clearAllTokens();
                Window.alert("All tokens cleared");
            }
        });
        RootPanel.get().add(button);
    }

    private RequestCallback getStandardRequestCallback() {
        return new RequestCallback() {
            @Override
            public void onResponseReceived(Request request, Response response) {
                Window.alert("Result: " + response.getText());
            }

            @Override
            public void onError(Request request, Throwable exception) {
                Window.alert("Error:\n" + exception.getMessage());
            }
        };
    }

    private static Callback<String, Throwable> getStandardCallback() {
        return new Callback<String, Throwable>() {
            @Override
            public void onSuccess(String result) {
                Window.alert("Result: " + result);
                currentToken = result;
            }

            @Override
            public void onFailure(Throwable caught) {
                Window.alert("Error:\n" + caught.getMessage());
            }
        };
    }
}
