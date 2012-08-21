package org.jboss.pressgangccms.oauth2.gwt.sample.client;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import org.jboss.pressgangccms.oauth2.gwt.client.AuthorisationRequest;
import org.jboss.pressgangccms.oauth2.gwt.client.Authoriser;
import org.jboss.pressgangccms.oauth2.gwt.client.OAuthHandler;
import org.jboss.pressgangccms.oauth2.gwt.client.OAuthRequest;

/**
 * Sample app demonstrating how OAuth2GwtClientProvider library can be used.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
public class App implements EntryPoint {

    private static final OAuthHandler AUTH_HANDLER = OAuthHandler.get();
    private static final String GOOGLE_PROVIDER_URL = "gmail.com";
    private static final String YAHOO_PROVIDER_URL = "yahoo.com";
    private static final String FACEBOOK_PROVIDER_URL = "www.facebook.com";
    private static final String MYOPENID_PROVIDER_PREFIX = "https://";
    private static final String MYOPENID_PROVIDER_SUFFIX = ".myopenid.com";
    private static final String FEDORA_PROVIDER_PREFIX = "https://admin.fedoraproject.org/accounts/openid/id/";
    private static final String RED_HAT_PROVIDER_URL = "https://localhost:8443/OpenIdProvider";
    private static final String SAMPLE_REST_APP_URL = "https://localhost:8443/OAuth2SecuredRestApp";
    private static final String PEOPLE_URL = SAMPLE_REST_APP_URL + "/rest/people";
    private static final String PERSON_1_URL = SAMPLE_REST_APP_URL + "/rest/people/1";
    private static final String PRESSGANGCCMS_AUTH_SERVER_URL = "https://localhost:8443/OAuth2AuthServer";
    private static final String PRESSGANGCCMS_USER_QUERY_URL = PRESSGANGCCMS_AUTH_SERVER_URL + "/rest/auth/identity/query";
    private static final String PRESSGANGCCMS_ASSOCIATE_URL = PRESSGANGCCMS_AUTH_SERVER_URL + "/rest/auth/identity/associate";
    private static final String PRESSGANGCCMS_MAKE_PRIMARY_URL = PRESSGANGCCMS_AUTH_SERVER_URL + "/rest/auth/identity/makePrimary";
    private static final String PRESSGANGCCMS_LOGIN_URL = PRESSGANGCCMS_AUTH_SERVER_URL + "/rest/auth/login";
    private static final String PRESSGANGCCMS_CLIENT_ID = "pressgangccms_id";
    private final String PROVIDER_PARAM_STRING = "?provider=";
    private final String USER_ID_PARAM_STRING = "?id=";
    private final String TOKEN_PARAM_STRING = "&oauth_token=";

    private static String currentToken;
    private final Label inputLabel = new Label("Input: ");
    private final TextBox inputTextBox = new TextBox();

    public void onModuleLoad() {
        Authoriser.export(); // This line is needed
        addInput();
        addRedHatLogin();
        addGoogleLogin();
        addYahooLogin();
        addFacebookLogin();
        addMyOpenIdLogin();
        addFedoraLogin();
        addGetPeople();
        addGetPerson();
        addAssociateIdentity();
        makeIdentityPrimary();
        getIdentityInfo();
        addClearTokens();
        addRequiredTable();
    }

    private void addInput() {
        RootPanel.get().add(inputLabel);
        inputTextBox.getElement().setId("inputTextBox");
        RootPanel.get().add(inputTextBox);
    }

    private void addRedHatLogin() {
        // Since the auth flow requires opening a popup window, it must be sthttp://oauth.net/2/arted
        // as a direct result of a user action, such as clicking a button or link.
        // Otherwise, a browser's popup blocker may block the popup.
        Button button = new Button("Login with Red Hat");
        button.getElement().setId("redHatLoginButton");
        button.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final AuthorisationRequest request = new AuthorisationRequest(PRESSGANGCCMS_LOGIN_URL + PROVIDER_PARAM_STRING
                        + RED_HAT_PROVIDER_URL, PRESSGANGCCMS_CLIENT_ID);
                AUTH_HANDLER.login(request, getStandardCallback());
            }
        });
        RootPanel.get().add(button);
    }

    private void addGoogleLogin() {
        Button button = new Button("Login with Google");
        button.getElement().setId("googleLoginButton");
        button.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final AuthorisationRequest request = new AuthorisationRequest(PRESSGANGCCMS_LOGIN_URL + PROVIDER_PARAM_STRING
                        + GOOGLE_PROVIDER_URL, PRESSGANGCCMS_CLIENT_ID);
                AUTH_HANDLER.login(request, getStandardCallback());
            }
        });
        RootPanel.get().add(button);
    }

    private void addYahooLogin() {
        Button button = new Button("Login with Yahoo");
        button.getElement().setId("yahooLoginButton");
        button.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final AuthorisationRequest request = new AuthorisationRequest(PRESSGANGCCMS_LOGIN_URL + PROVIDER_PARAM_STRING
                        + YAHOO_PROVIDER_URL, PRESSGANGCCMS_CLIENT_ID);
                AUTH_HANDLER.login(request, getStandardCallback());
            }
        });
        RootPanel.get().add(button);
    }

    private void addFacebookLogin() {
        Button button = new Button("Login with Facebook");
        button.getElement().setId("facebookLoginButton");
        button.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Window.alert("Not yet implemented!");
//                final AuthorisationRequest request = new AuthorisationRequest(PRESSGANGCCMS_LOGIN_URL + PROVIDER_PARAM_STRING
//                        + FACEBOOK_PROVIDER_URL, PRESSGANGCCMS_TOKEN_URL,
//                        PRESSGANGCCMS_CLIENT_ID, PRESSGANGCCMS_CLIENT_SECRET);
//                AUTH_HANDLER.login(request, getStandardCallback());
            }
        });
        RootPanel.get().add(button);
    }

    private void addMyOpenIdLogin() {
        Button button = new Button("Login with MyOpenID.com*");
        button.getElement().setId("myOpenIdLoginButton");
        button.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final AuthorisationRequest request = new AuthorisationRequest(PRESSGANGCCMS_LOGIN_URL + PROVIDER_PARAM_STRING
                        + MYOPENID_PROVIDER_PREFIX + inputTextBox.getText() + MYOPENID_PROVIDER_SUFFIX, PRESSGANGCCMS_CLIENT_ID);
                AUTH_HANDLER.login(request, getStandardCallback());
            }
        });
        RootPanel.get().add(button);
    }

    private void addFedoraLogin() {
        Button button = new Button("Login with Fedora Account System*");
        button.getElement().setId("fedoraLoginButton");
        button.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final AuthorisationRequest request = new AuthorisationRequest(PRESSGANGCCMS_LOGIN_URL + PROVIDER_PARAM_STRING
                        + FEDORA_PROVIDER_PREFIX + inputTextBox.getText(), PRESSGANGCCMS_CLIENT_ID);
                AUTH_HANDLER.login(request, getStandardCallback());
            }
        });
        RootPanel.get().add(button);
    }

    private void addGetPeople() {
        Button button = new Button("GET all people");
        button.getElement().setId("getAllPeopleButton");
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
        button.getElement().setId("getPersonButton");
        button.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                AUTH_HANDLER.sendRequest(new OAuthRequest(RequestBuilder.GET, PERSON_1_URL), getStandardRequestCallback());
            }
        });
        RootPanel.get().add(button);
    }

    private void addAssociateIdentity() {
        Button button = new Button("Associate provider identity*");
        button.getElement().setId("associateProviderIdentityButton");

        button.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final AuthorisationRequest request = new AuthorisationRequest(PRESSGANGCCMS_ASSOCIATE_URL + PROVIDER_PARAM_STRING
                        + inputTextBox.getText() + TOKEN_PARAM_STRING + currentToken, PRESSGANGCCMS_CLIENT_ID).forceNewRequest(true);
                AUTH_HANDLER.login(request, getStandardCallback());
            }
        });
        RootPanel.get().add(button);
    }

    private void makeIdentityPrimary() {
        Button button = new Button("Make identity primary*");
        button.getElement().setId("makeIdentityPrimaryButton");
        button.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final AuthorisationRequest request = new AuthorisationRequest(PRESSGANGCCMS_MAKE_PRIMARY_URL
                        + USER_ID_PARAM_STRING + AUTH_HANDLER.encodeUrl(inputTextBox.getText()) + TOKEN_PARAM_STRING + currentToken,
                        PRESSGANGCCMS_CLIENT_ID).forceNewRequest(true);
                AUTH_HANDLER.login(request, getStandardCallback());
            }
        });
        RootPanel.get().add(button);
    }

    private void getIdentityInfo() {
        Button button = new Button("GET identity info");
        button.getElement().setId("getIdentityInfoButton");
        button.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                AUTH_HANDLER.sendRequest(new OAuthRequest(RequestBuilder.GET, PRESSGANGCCMS_USER_QUERY_URL), getStandardRequestCallback());
            }
        });
        RootPanel.get().add(button);
    }

    private void addRequiredTable() {
        Label label = new Label("*Requires input");
        HTMLTable table = new Grid(5, 2);
        table.setText(0, 0, "**Button**");
        table.setText(0, 1, "**Value required**");
        table.setText(1, 0, "Login with MyOpenID.com");
        table.setText(1, 1, "Username from OpenID identifier ie: {username}.myopenid.com");
        table.setText(2, 0, "Login with Fedora Account System");
        table.setText(2, 1, "Username from OpenID identifier ie: https://admin.fedoraproject.org/accounts/openid/id/{username}");
        table.setText(3, 0, "Associate provider identity");
        table.setText(3, 1, "URL/domain of OpenID provider to associate ie: gmail.com");
        table.setText(4, 0, "Make identity primary");
        table.setText(4, 1, "OpenID identifier to make primary ie: https://admin.fedoraproject.org/accounts/openid/id/johnsmith");
        RootPanel.get().add(label);
        RootPanel.get().add(table);
    }

    // Clears all tokens stored in the browser by this library. Subsequent calls
    // to authorise() will result in the popup being shown, though it may immediately
    // disappear if the token has not expired.
    private void addClearTokens() {
        Button button = new Button("Clear stored tokens");
        button.getElement().setId("clearStoredTokensButton");
        button.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                OAuthHandler.get().clearAllTokens();
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
