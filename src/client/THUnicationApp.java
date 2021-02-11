package client;


import client.logic.ApplicationController;
import client.logic.ApplicationModel;
import client.network.ClientNetwork;
import client.ui.ApplicationView;
import javafx.application.Application;
import javafx.stage.Stage;

public class THUnicationApp extends Application {

    private static ClientNetwork clientNetwork;
    private static ApplicationModel model;
    private static ApplicationView applicationView;

    @Override
    public void start(Stage primaryStage) throws Exception {
        model = new ApplicationModel();
        applicationView = new ApplicationView();
        applicationView.setModel(model);
        clientNetwork = new ClientNetwork(System.getenv("THUNICATION_SERVER_ADDRESS"), Integer.parseInt(System.getenv("THUNICATION_SERVER_PORT")));

        if (clientNetwork.isConnected()) {
            applicationView.start(primaryStage);
        } else {
            applicationView.showNoServerConnection();
        }
    }

    @Override
    public void stop() {
        try {
            super.stop();
            clientNetwork.stop(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        launch(args);
    }

    public static ClientNetwork getClientNetwork() {
        return clientNetwork;
    }

    public static ApplicationModel getModel() {
        return model;
    }

    public static ApplicationView getApplicationView() {
        return applicationView;
    }
}
