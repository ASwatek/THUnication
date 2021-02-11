package client.logic;

import client.THUnicationApp;
import client.network.ClientNetwork;
import client.ui.ApplicationView;
import domain.user.User;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import network.LoginCredentials;
import network.NetworkPackage;
import network.NetworkPackageHandler;
import network.RegisterData;

import java.net.URL;
import java.util.ResourceBundle;

public class RegistryController implements Initializable, NetworkPackageHandler {

    private ApplicationView view;
    private ApplicationModel model;
    private ClientNetwork network;

    @FXML
    private TextField username;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private CheckBox teacherButton;

    /**
     * Called after UI is created.
     * Network, view, model will be set.
     * @param url not relevant, but predefined
     * @param resourceBundle not relevant, but predefined
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
     * If no connection is available, the app will be closed.
     */
    @Override
    public void networkConnectionLost() {
        Platform.runLater(() -> view.showNoServerConnection());
    }

    /**
     * Triggers by registry button and try to create a new user.
     */
    @FXML
    public void clickOnRegistryAgain() {
        if (passwordField.getText().isEmpty() || username.getText().isEmpty() || confirmPasswordField.getText().isEmpty()) {
            view.showErrorMessage("Registration failed", "Registration failed", "You have to fill out every field.");
            return;
        }

        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            view.showErrorMessage("Registration failed", "Registration failed", "Your entered passwords are not equal.");
            return;
        }

        String role = "Student";
        if (teacherButton.isSelected()) {
            role = "Teacher";
        }

        LoginCredentials credentials = new RegisterData(username.getText(), passwordField.getText(), role);
        network.sendPackage(new NetworkPackage(NetworkPackage.Type.REGISTER, credentials));
    }

    /**
     * Back to the login mode.
     * @throws Exception In the case that starting login mode is not working.
     */
    @FXML
    public void clickBack() throws Exception {
        view.startLogin();
    }

    /**
     * Handles registry answer from the server
     * @param networkPackage network data
     */
    @Override
    public void handlePackage(NetworkPackage networkPackage) {
        if (networkPackage.getType() == NetworkPackage.Type.REGISTER) {
            if (networkPackage.hasValidationError()) {
                Platform.runLater(() -> view.showErrorMessage("Registration failed", "Registration failure.", networkPackage.getErrorMessage()));
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
