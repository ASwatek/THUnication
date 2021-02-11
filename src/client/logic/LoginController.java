package client.logic;

import client.THUnicationApp;
import client.network.ClientNetwork;
import client.ui.ApplicationView;
import domain.user.User;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import network.LoginCredentials;
import network.NetworkPackage;
import network.NetworkPackageHandler;

import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable, NetworkPackageHandler {

    private ApplicationView view;
    private ApplicationModel model;
    private ClientNetwork network;

    @FXML
    private TextField username;

    @FXML
    private PasswordField passwordField;

    /**
     * Called after the UI is created
     * @param url not relevant but predefined
     * @param resourceBundle not relevant but predefined
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        network = THUnicationApp.getClientNetwork();
        view = THUnicationApp.getApplicationView();
        model = THUnicationApp.getModel();
        if (network != null) {
            network.setPackageHandler(this);
        }
    }


    /**
     * App will be closed when there is no connection available
     */
    @Override
    public void networkConnectionLost() {
        Platform.runLater(() -> view.showNoServerConnection());
    }


    /**
     * Triggers by login button and tries to log in.
     */
    @FXML
    public void clickOnLogin() {
        if (passwordField.getText().isEmpty() || username.getText().isEmpty()) {
            view.showErrorMessage("Login failed", "Login failure", "You have to fill out every field.");
            return;
        }

        LoginCredentials credentials = new LoginCredentials(username.getText(), passwordField.getText());
        network.sendPackage(new NetworkPackage(NetworkPackage.Type.LOGIN, credentials));
    }

    /**
     * Triggers by registry button and go to the registry mode
     * @throws Exception In the case that starting registry is not working
     */
    @FXML
    public void clickOnRegistry() throws Exception {
        view.startRegistry();
    }

    /**
     * Handles answer of the server for login. If it is successful, the app will started.
     * If not a error will be shown.
     * @param networkPackage network data
     */
    @Override
    public void handlePackage(NetworkPackage networkPackage) {
        if (networkPackage.getType() == NetworkPackage.Type.LOGIN) {
            if (networkPackage.hasValidationError()) {
                Platform.runLater(() -> view.showErrorMessage("Login failed", "Login failure.", networkPackage.getErrorMessage()));
                return;
            }

            User user = (User) networkPackage.getContent();
            model.setCurrentUser(user);
            System.out.println("User " + user.getUsername() + " is authenticated.");

            // registration/login was successful -> start app
            Platform.runLater(() -> {
                try {
                    view.startApp();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }


}
