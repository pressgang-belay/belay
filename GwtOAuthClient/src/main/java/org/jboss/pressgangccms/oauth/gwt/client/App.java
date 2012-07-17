package org.jboss.pressgangccms.oauth.gwt.client;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.*;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.RootPanel;
import org.jboss.pressgangccms.oauth.gwt.client.oauth.AuthorisationRequest;
import org.jboss.pressgangccms.oauth.gwt.client.oauth.Authoriser;
import org.jboss.pressgangccms.oauth.gwt.client.oauth.OAuthHandler;
import org.jboss.pressgangccms.oauth.gwt.client.oauth.OAuthRequest;

public class App implements EntryPoint {

    // Use the implementation of Auth intended to be used in the GWT client app.
    private static final OAuthHandler AUTH_HANDLER = OAuthHandler.get();
    private static final String PEOPLE_URL = "https://localhost:8443/OAuthProvider/rest/people";
    private static final String SKYNET_LOGIN_URL = "https://localhost:8443/OAuthProvider/rest/auth/login";
    private static final String SKYNET_TOKEN_URL = "https://localhost:8443/OAuthProvider/rest/auth/token";
    private static final String RED_HAT_PROVIDER_URL = "https://localhost:8443/OpenIdProvider/";
    private static final String GOOGLE_PROVIDER_URL = "gmail.com";
    // This app's personal client ID assigned by the Skynet OAuth server
    private static final String SKYNET_CLIENT_ID = "affbf16ab449cfa1e16392f705f9460";

    public void onModuleLoad() {
        Authoriser.export();
        addRedHatLogin();
        addGoogleLogin();
        addGetPeople();
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
                final AuthorisationRequest request = new AuthorisationRequest(SKYNET_LOGIN_URL, SKYNET_TOKEN_URL,
                        SKYNET_CLIENT_ID, RED_HAT_PROVIDER_URL);
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
                final AuthorisationRequest request = new AuthorisationRequest(SKYNET_LOGIN_URL, SKYNET_TOKEN_URL,
                        SKYNET_CLIENT_ID, GOOGLE_PROVIDER_URL);
                AUTH_HANDLER.login(request, getStandardCallback());
            }
        });
        RootPanel.get().add(button);
    }

    private void addGetPeople() {
        Button button = new Button("GET all people");
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

    private void addRefresh() {
        Button button = new Button("Force token refresh");
        button.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final AuthorisationRequest request = new AuthorisationRequest(SKYNET_LOGIN_URL, SKYNET_TOKEN_URL,
                        SKYNET_CLIENT_ID, RED_HAT_PROVIDER_URL);
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

    private static Callback<String, Throwable> getStandardCallback() {
        return new Callback<String, Throwable>() {
            @Override
            public void onSuccess(String result) {
                Window.alert("Result: " + result);
            }

            @Override
            public void onFailure(Throwable caught) {
                Window.alert("Error:\n" + caught.getMessage());
            }
        };
    }
}
