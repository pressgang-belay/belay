package org.jboss.pressgang.belay.oauth2.gwt.sample.client;

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
import org.jboss.pressgang.belay.oauth2.gwt.client.AuthorizationRequest;
import org.jboss.pressgang.belay.oauth2.gwt.client.OAuthHandler;
import org.jboss.pressgang.belay.oauth2.gwt.client.OAuthRequest;
import org.jboss.pressgang.belay.oauth2.gwt.client.OpenIdRequestUtil;

/**
 * Sample app demonstrating how OAuth2GwtClientProvider library can be used.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
public class App implements EntryPoint {

    private static final OAuthHandler AUTH_HANDLER = OAuthHandler.get();
    private static final String GOOGLE_PROVIDER_URL = "gmail.com";
    private static final String YAHOO_PROVIDER_URL = "me.yahoo.com";
    private static final String FACEBOOK_PROVIDER_URL = "www.facebook.com";
    private static final String MYOPENID_PROVIDER_PREFIX = "https://";
    private static final String MYOPENID_PROVIDER_SUFFIX = ".myopenid.com";
    private static final String FEDORA_PROVIDER_PREFIX = "https://admin.fedoraproject.org/accounts/openid/id/";
    private static final String RED_HAT_PROVIDER_URL = "https://localhost:8443/OpenIdProvider";
    private static final String SAMPLE_REST_APP_URL = "https://localhost:8443/OAuth2SecuredRestApp";
    private static final String PEOPLE_URL = SAMPLE_REST_APP_URL + "/rest/people";
    private static final String PERSON_1_URL = SAMPLE_REST_APP_URL + "/rest/people/1";
    private static final String BELAY_AUTH_SERVER_URL = "https://localhost:8443/OAuth2AuthServer";
    private static final String BELAY_IDENTITY_QUERY_URL = BELAY_AUTH_SERVER_URL + "/rest/auth/user/queryIdentity";
    private static final String BELAY_USER_QUERY_URL = BELAY_AUTH_SERVER_URL + "/rest/auth/user/queryUser";
    private static final String BELAY_ASSOCIATE_URL = BELAY_AUTH_SERVER_URL + "/rest/auth/user/associate/associateIdentity";
    private static final String BELAY_MAKE_PRIMARY_URL = BELAY_AUTH_SERVER_URL + "/rest/auth/user/makeIdentityPrimary";
    private static final String BELAY_AUTH_URL = BELAY_AUTH_SERVER_URL + "/rest/auth/authorize";
    private static final String BELAY_CLIENT_ID = "pressgang_belay_id";

    private static String currentToken;
    private final Label inputLabel = new Label("Input: ");
    private final TextBox inputTextBox = new TextBox();

    public void onModuleLoad() {
        addInput();
        addRedHatLogin();
        addGoogleLogin();
        addYahooLogin();
        //addFacebookLogin();
        addMyOpenIdLogin();
        addFedoraLogin();
        addGetPeople();
        addGetPerson();
        addAssociateIdentity();
        makeIdentityPrimary();
        getIdentityInfo();
        getUserInfo();
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
                final AuthorizationRequest request = OpenIdRequestUtil.openIdAuthorizationRequest(BELAY_AUTH_URL,
                        BELAY_CLIENT_ID, RED_HAT_PROVIDER_URL).withScopes("PERFORM_USER_MANAGEMENT");
                AUTH_HANDLER.sendAuthRequest(request, getStandardCallback());
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
                final AuthorizationRequest request = OpenIdRequestUtil.openIdAuthorizationRequest(BELAY_AUTH_URL,
                        BELAY_CLIENT_ID, GOOGLE_PROVIDER_URL).withScopes("PERFORM_USER_MANAGEMENT");
                AUTH_HANDLER.sendAuthRequest(request, getStandardCallback());
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
                final AuthorizationRequest request = OpenIdRequestUtil.openIdAuthorizationRequest(BELAY_AUTH_URL,
                        BELAY_CLIENT_ID, YAHOO_PROVIDER_URL);
                AUTH_HANDLER.sendAuthRequest(request, getStandardCallback());
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
                final AuthorizationRequest request = OpenIdRequestUtil.openIdAuthorizationRequest(BELAY_AUTH_URL,
                        BELAY_CLIENT_ID, MYOPENID_PROVIDER_PREFIX + inputTextBox.getText() + MYOPENID_PROVIDER_SUFFIX)
                        .withScopes("PERFORM_USER_MANAGEMENT");
                AUTH_HANDLER.sendAuthRequest(request, getStandardCallback());
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
                final AuthorizationRequest request = OpenIdRequestUtil.openIdAuthorizationRequest(BELAY_AUTH_URL,
                        BELAY_CLIENT_ID, FEDORA_PROVIDER_PREFIX + inputTextBox.getText())
                        .withScopes("PERFORM_USER_MANAGEMENT");
                AUTH_HANDLER.sendAuthRequest(request, getStandardCallback());
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
                AUTH_HANDLER.sendRequest(new OAuthRequest(RequestBuilder.GET, PEOPLE_URL), new RequestCallback() {
                    @Override
                    public void onResponseReceived(Request request, Response response) {
                        Window.alert("Result: " + response.getText());
                    }

                    @Override
                    public void onError(Request request, Throwable exception) {
                        Window.alert("Error:\n" + exception.getMessage());
                    }
                });
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
                final AuthorizationRequest request = OpenIdRequestUtil.associateIdentityRequest(BELAY_ASSOCIATE_URL,
                        BELAY_CLIENT_ID, inputTextBox.getText(), AUTH_HANDLER.getLastTokenResult(), false)
                        .withScopes("PERFORM_USER_MANAGEMENT");
                AUTH_HANDLER.sendAuthRequest(request, getStandardCallback());
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
                AUTH_HANDLER.sendRequest(OpenIdRequestUtil.makeIdentityPrimaryRequest(RequestBuilder.GET,
                        BELAY_MAKE_PRIMARY_URL, inputTextBox.getText()), getStandardRequestCallback());
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
                AUTH_HANDLER.sendRequest(new OAuthRequest(RequestBuilder.GET, BELAY_IDENTITY_QUERY_URL), getStandardRequestCallback());
            }
        });
        RootPanel.get().add(button);
    }

    private void getUserInfo() {
        Button button = new Button("GET user info");
        button.getElement().setId("getUserInfoButton");
        button.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                AUTH_HANDLER.sendRequest(new OAuthRequest(RequestBuilder.GET, BELAY_USER_QUERY_URL), getStandardRequestCallback());
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
    // to authorize() will result in the popup being shown, though it may immediately
    // disappear if the token has not expired.
    private void addClearTokens() {
        Button button = new Button("Clear stored tokens");
        button.getElement().setId("clearStoredTokensButton");
        button.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                AUTH_HANDLER.clearAllTokens();
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
