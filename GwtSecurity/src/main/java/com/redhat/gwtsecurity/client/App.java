package com.redhat.gwtsecurity.client;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.*;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.RootPanel;
import com.redhat.gwtsecurity.client.oauth.AuthorisationRequest;
import com.redhat.gwtsecurity.client.oauth.Authoriser;
import com.redhat.gwtsecurity.client.oauth.OAuthHandler;

public class App implements EntryPoint {

    // Use the implementation of Auth intended to be used in the GWT client app.
    private static final OAuthHandler AUTH_HANDLER = OAuthHandler.get();
    private static final String PEOPLE_URL = "https://localhost:8443/RestSecurity/rest/people";
    private static final String SKYNET_LOGIN_URL = "https://localhost:8443/RestSecurity/rest/auth/login";
    private static final String RED_HAT_PROVIDER_URL = "https://localhost:8443/OpenIdProvider/";
    private static final String GOOGLE_PROVIDER_URL = "gmail.com";
    // This app's personal client ID assigned by the Skynet OAuth page
    private static final String SKYNET_CLIENT_ID = "skynet_id"; // TODO

    private static String accessToken;

    public void onModuleLoad() {
        addRedHatLogin();
        addGoogleLogin();
        addClearTokens();
        Authoriser.export();  //TODO where best to put this ?
    }

//    private void addLogin() {
//
//        Label usernameLabel = new Label("Username:");
//        final TextBox textBox = new TextBox();
//        textBox.setName("username");
//        textBox.setMaxLength(25);
//        Label passwordLabel = new Label("Password:");
//        final PasswordTextBox passwordTextBox = new PasswordTextBox();
//        passwordTextBox.setName("password");
//        passwordTextBox.setMaxLength(25);
//        Button loginButton = new Button("Login", new ClickHandler() {
//            @Override
//            public void onClick(ClickEvent event) {
//                doLogin(textBox.getText(), passwordTextBox.getText());
//            }
//        });
//
//        VerticalPanel panel = new VerticalPanel();
//        panel.add(usernameLabel);
//        panel.add(textBox);
//        panel.add(passwordLabel);
//        panel.add(passwordTextBox);
//        panel.add(loginButton);
//
//        RootPanel.get().add(panel);
//    }

    private void sendPeopleRequest(String token) {

        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, PEOPLE_URL);
        builder.setHeader("Authorization", "OAuth " + "oauth_signature_method " + "Bearer " + token);

        try {
            builder.sendRequest(null, new RequestCallback() {

                @Override
                public void onResponseReceived(Request request, Response response) {
                    Window.alert("Response code: " + response.getStatusCode()
                            + " Response status: " + response.getStatusText()
                            + " Response headers: " + response.getHeadersAsString());
                }

                @Override
                public void onError(Request request, Throwable exception) {
                    Window.alert("Error!");
                }
            });
        } catch(
                RequestException re) {
            Window.alert(re.getMessage());
        }
    }

    private void addRedHatLogin() {
        // Since the auth flow requires opening a popup window, it must be started
        // as a direct result of a user action, such as clicking a button or link.
        // Otherwise, a browser's popup blocker may block the popup.
        Button button = new Button("Login with Red Hat");
        button.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final AuthorisationRequest req = new AuthorisationRequest(SKYNET_LOGIN_URL, SKYNET_CLIENT_ID, RED_HAT_PROVIDER_URL);
                AUTH_HANDLER.login(req, new Callback<String, Throwable>() {
                    @Override
                    public void onSuccess(String result) {
                        Window.alert("Result: " + result + "\n"
                                + "Token expires in " + AUTH_HANDLER.expiresIn(req) + " ms\n");
                        // sendPeopleRequest(token);
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        Window.alert("Error:\n" + caught.getMessage());
                    }
                });
            }
        });
        RootPanel.get().add(button);
    }

    private void addGoogleLogin() {
        // Since the auth flow requires opening a popup window, it must be started
        // as a direct result of a user action, such as clicking a button or link.
        // Otherwise, a browser's popup blocker may block the popup.
        Button button = new Button("Login with Google");
        button.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final AuthorisationRequest req = new AuthorisationRequest(SKYNET_LOGIN_URL, SKYNET_CLIENT_ID, GOOGLE_PROVIDER_URL);
                AUTH_HANDLER.login(req, new Callback<String, Throwable>() {
                    @Override
                    public void onSuccess(String result) {
                        Window.alert("Result: " + result + "\n"
                                + "Token expires in " + AUTH_HANDLER.expiresIn(req) + " ms\n");
                        // sendPeopleRequest(token);
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        Window.alert("Error:\n" + caught.getMessage());
                    }
                });
            }
        });
        RootPanel.get().add(button);
    }

    // //////////////////////////////////////////////////////////////////////////
    // CLEARING STORED TOKENS ///////////////////////////////////////////////////
    // //////////////////////////////////////////////////////////////////////////

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
}
