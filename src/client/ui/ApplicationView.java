package client.ui;

import client.logic.ApplicationModel;
import domain.Conversation;
import domain.message.Message;
import domain.user.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Dimension2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.html.HTMLAnchorElement;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class ApplicationView {

    private Stage primaryStage;
    private ApplicationModel model;
    private HtmlClassHelper chatHtmlHelper;
    private SimpleSVGHTMLClass svghelper = new SimpleSVGHTMLClass();
    private ObservableList<Conversation> observableList;
    private int leftSidebarButtonState = 0;


    /**
     * Starts login mode and add the icon.
     * @param stage what is shown in the display
     * @throws Exception In the case when something go wrong
     */
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("resources/icons/THUnication.png")));

        primaryStage.setTitle("THUnication");
        startLogin();
    }

    /**
     * loads the login UI
     * @throws Exception In the case when something go wrong
     */
    @FXML
    public void startLogin() throws Exception {
        initApp("resources/login.fxml", false);
    }

    /**
     * Shows a message to the user when something wrong happened
     * @param title of the message
     * @param header of the message
     * @param errorMessage content of error
     */
    public void showErrorMessage(String title, String header, String errorMessage) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(errorMessage);
        alert.initOwner(primaryStage);
        alert.showAndWait();
    }

    /**
     * loads the app UI
     * @throws Exception In the case when something go wrong
     */
    public void startApp() throws Exception {
        initApp("resources/application.fxml", true);
        primaryStage.setMaximized(true);
    }

    /**
     * loads the registry UI
     * @throws Exception In the case when something go wrong
     */
    @FXML
    public void startRegistry() throws Exception {
        initApp("resources/registry.fxml", false);
    }

    /**
     * loads a fxml file
     * @param path which layout should be loaded
     * @param resizable resizability of the ui
     * @throws Exception In the case when something go wrong
     */
    private void initApp(String path, boolean resizable) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(path));

        Parent root = loader.load();

        primaryStage.setResizable(resizable);
        if (primaryStage.getScene() == null)
            primaryStage.setScene(new Scene(root));
        else
            primaryStage.getScene().setRoot(root);
        primaryStage.show();
    }

    /**
     * Set the model inside the view
     * @param model model which is used in the view
     */
    public void setModel(ApplicationModel model) {
        this.model = model;
    }

    /**
     * If there is no connection available, a alert message will be shown.
     */
    public void showNoServerConnection() {
        if (primaryStage != null) {
            primaryStage.close();
        }

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("No connection to server.");
        alert.setHeaderText("Network Error");
        alert.setContentText("THUnication can't establish a connection to server.");
        alert.showAndWait();
    }

    /**
     * @param message will added in the chat
     */
    public void addNewMessage(Message message) {
        chatHtmlHelper.addNewMessage(message);
    }

    /**
     * @param messages will added in the chat
     */
    public void addMessages(Message[] messages) {
        chatHtmlHelper.addMessages(messages);
    }

    /**
     * Chat content will be loaded
     * WebView will be initialized
     * @param chatWebView will be initialize
     */
    private void initWebViews(WebView chatWebView) {
        chatHtmlHelper = new HtmlClassHelper(model.getCurrentUser());
        updateApp(chatWebView);

        // The context menu provides functions like 'reload page' or 'open in new tab'
        // both things we don't want, so it's best to disable it completely.
        chatWebView.setContextMenuEnabled(false);

        // Prevent opening links which are displayed inside the webview from opening inside the webview.
        chatWebView.getEngine().getLoadWorker().stateProperty().addListener((observableValue, state, t1) -> {
            if (observableValue.getValue().equals(Worker.State.SUCCEEDED)) {
                NodeList nodeList = chatWebView.getEngine().getDocument().getElementsByTagName("a");
                for (int i = 0; i < nodeList.getLength(); i++) {
                    org.w3c.dom.Node node = nodeList.item(i);
                    EventTarget eventTarget = (EventTarget) node;
                    eventTarget.addEventListener("click", event -> {
                        HTMLAnchorElement anchorElement = (HTMLAnchorElement) event.getCurrentTarget();

                        // Try to open link in default browser.
                        if (Desktop.isDesktopSupported()) {
                            try {
                                URI uri = new URI(anchorElement.getHref());
                                Desktop.getDesktop().browse(uri);
                            } catch (URISyntaxException | IOException e) {
                                // Ignore any failure, it's most likely a malformed url.
                            }
                        }
                        event.preventDefault();
                    }, false);
                }
            }
        });
    }

    /**
     * Loads content of the chat
     * @param chatWebView content of the chat
     */
    public void updateApp(WebView chatWebView) {
        WebEngine webEngine = chatWebView.getEngine();
        webEngine.loadContent(chatHtmlHelper.getHtmlFullString(), "text/html");
    }

    /**
     * Initialize list of conversations
     * @param listView list of conversations
     */
    public void initListView(ListView<Conversation> listView) {
        observableList = FXCollections.observableArrayList();
        observableList.addAll(model.getConversationAsList());
        listView.setItems(observableList);
    }

    /**
     * Add new conversation into the list
     * @param conv new conversation
     * @param webView list of conversations
     */
    public void loadNewConversation(Conversation conv, WebView webView) {
        chatHtmlHelper = new HtmlClassHelper(model.getCurrentUser());
        chatHtmlHelper.addMessages(conv.getMessages());
        updateApp(webView);
    }

    /**
     * Push conversation to the top
     * @param conv existing conversation
     */
    public void convToTheTop(Conversation conv) {
        observableList.remove(conv);
        observableList.add(0, conv);
    }

    /**
     * new conversation will be added in the list
     * @param conv new conversation
     */
    public void addNewConv(Conversation conv) {
        observableList.add(conv);
    }

    /**
     * Initialize the list of all users for the add conversation mode
     * @param listView list of users (UI)
     * @param users array of all users
     */
    public void initAddConvListView(ListView<User> listView, User[] users) {
        ObservableList<User> observableList = FXCollections.observableArrayList();
        observableList.addAll(Arrays.asList(users));
        observableList.remove(model.getCurrentUser());
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listView.setCellFactory(param -> new ListCell<>() {

            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                initUserList(item,empty,this,35);
            }
        });
        listView.setItems(observableList);
    }

    /**
     * Initialize the complete app UI
     * @param chatWebView content of the chat
     * @param convListView list of conversations
     * @param convBtn button for conversation mode
     * @param addConvBtn button for add conversation mode
     * @param editConversationBtn button for edit permissions in a group chat
     * @param showParticipantsBtn button for showing all participants in a group chat
     */
    public void initAppWebViews(WebView chatWebView, ListView<Conversation> convListView, WebView convBtn, WebView addConvBtn, WebView editConversationBtn, WebView showParticipantsBtn) {
        initWebViews(chatWebView);
        initListView(convListView);
        toggleButtonState(convBtn, addConvBtn);
        editConversationBtn.getEngine().loadContent(svghelper.getHTMLImage(SimpleSVGHTMLClass.IMAGE.EDITBTN), "text/html");
        showParticipantsBtn.getEngine().loadContent(svghelper.getHTMLImage(SimpleSVGHTMLClass.IMAGE.PARTICIPANTBTN), "text/html");
    }

    /**
     * Toggles modes in every event
     * @param convBtn conversation mode button
     * @param addConvBtn add conversation mode button
     */
    public void toggleButtonState(WebView convBtn, WebView addConvBtn) {
        if (leftSidebarButtonState == 0) {
            convBtn.getEngine().loadContent(svghelper.getHTMLImage(SimpleSVGHTMLClass.IMAGE.CONV_BTN_PRESSED), "text/html");
            addConvBtn.getEngine().loadContent(svghelper.getHTMLImage(SimpleSVGHTMLClass.IMAGE.ADD_CONV_BTN), "text/html");
            leftSidebarButtonState = 1;
        } else {
            convBtn.getEngine().loadContent(svghelper.getHTMLImage(SimpleSVGHTMLClass.IMAGE.CONV_BTN), "text/html");
            addConvBtn.getEngine().loadContent(svghelper.getHTMLImage(SimpleSVGHTMLClass.IMAGE.ADD_CONV_BTN_PRESSED), "text/html");
            leftSidebarButtonState = 0;
        }
    }

    /**
     * Initialize edit permission UI
     * @return result of this edit dialog after closing it
     */
    public Optional<Set<Integer>> createEditDialog() {
        Dialog<Set<Integer>> dialog = new Dialog<>();
        dialog.setTitle("Change Permissions");
        dialog.setHeaderText("Change Write Permissions");
        dialog.setGraphic(new ImageView(this.getClass().getResource("../ui/resources/icons/edit.png").toString()));
        dialog.initOwner(primaryStage);
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("../ui/resources/styles/thunication.css").toExternalForm());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);

        GridPane grid = new GridPane();

        ListView<User> userListView = new ListView<>();
        userListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        ObservableList<User> observableList = FXCollections.observableArrayList();
        observableList.addAll(model.getConversation(model.getActualConversationID()).getParticipants());

        userListView.setCellFactory(userListView1 -> new ListCell<>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                initUserList(item, empty, this, 24);
            }
        });
        userListView.setItems(observableList);

        observableList.forEach(user -> {
            if (model.getConversation(model.getActualConversationID()).getCanWritePermissions().contains(user.getId())) {
                userListView.getSelectionModel().select(user);
            }
        });

        grid.add(userListView, 0, 0);
        grid.add(new Label("Mark the users who should be able to write messages."), 0, 1);

        ColumnConstraints constraints = new ColumnConstraints();
        constraints.setMinWidth(400.0);
        grid.getColumnConstraints().add(constraints);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setMinWidth(400.0);


        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                Set<Integer> userIds = new HashSet<>();
                userListView.getSelectionModel().getSelectedItems().forEach(user -> userIds.add(user.getId()));
                return userIds;
            }
            return null;
        });
        return dialog.showAndWait();
    }

    private void initUserList(User item, boolean empty, ListCell<User> listCell, int size) {
        if (empty || item == null) {
            listCell.setText(null);
            listCell.setGraphic(null);
        } else {
            Canvas canvas = new Canvas(size, size);
            drawAvatar(canvas, item.getUsername());
            listCell.setGraphic(canvas);
            listCell.setFont(Font.font("Calibri", 17));
            listCell.setText(item.getUsername());
        }
    }

    /**
     * Initialize show participants UI
     */
    public void createShowParticipantDialog() {
        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("List Of Participants");
        dialog.initOwner(primaryStage);
        dialog.setHeaderText("Participants");
        dialog.setGraphic(new ImageView(this.getClass().getResource("../ui/resources/icons/users.png").toString()));
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("resources/styles/thunication.css").toExternalForm());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);

        ListView<User> userListView = new ListView<>();
        userListView.setFocusTraversable(false);
        userListView.setSelectionModel(new NoSelectionModel<>());

        ObservableList<User> observableList = FXCollections.observableArrayList();
        observableList.addAll(model.getConversation(model.getActualConversationID()).getParticipants());

        userListView.setCellFactory(userListView1 -> new ListCell<>() {
            @Override
            protected void updateItem(User item, boolean empty) {
               initUserList(item,empty,this,24);
            }
        });
        userListView.setItems(observableList);

        dialog.getDialogPane().setContent(userListView);
        dialog.getDialogPane().setMinWidth(400.0);
        dialog.showAndWait();
    }

    /**
     * draw a avatar with canvas.
     * @param avatarImg the canvas which has to be modified
     * @param name where canvas take the first letter in upper case
     */
    public void drawAvatar(Canvas avatarImg, String name) {
        GraphicsContext graphicsContext = avatarImg.getGraphicsContext2D();


        double sizePerPercent = avatarImg.getWidth() / 100.0;
        double yProportional = (76 * sizePerPercent);

        double red = 0;
        double green = 0;
        double blue = 0;

        if (name.hashCode() != 0) {
            red = Math.abs(name.hashCode() / 3) % 256;
            green = Math.abs(name.hashCode() / 5) % 256;
            blue = Math.abs(name.hashCode() / 7) % 256;
        }


        graphicsContext.setFill(Color.color(red / 256.0, green / 256.0, blue / 256.0));
        graphicsContext.fillOval(0, 0, avatarImg.getWidth(), avatarImg.getHeight());
        graphicsContext.setFill(Color.WHITE);
        graphicsContext.setFont(Font.font("Calibri", yProportional));
        Dimension2D dimension2D;
        char symbol = name.toUpperCase().charAt(0);
        if (symbol == 'W' || symbol == 'M') {
            dimension2D = new Dimension2D(14 * sizePerPercent, yProportional);
        } else if (symbol == 'I' || symbol == 'J') {
            dimension2D = new Dimension2D(40 * sizePerPercent, yProportional);
        } else if (symbol == 'H' || symbol == 'Q' || symbol == 'O') {
            dimension2D = new Dimension2D(26 * sizePerPercent, yProportional);
        } else if (symbol == 'B' || symbol == 'U' || symbol == 'G' || symbol == 'N' || symbol == 'C') {
            dimension2D = new Dimension2D(28 * sizePerPercent, yProportional);
        } else {
            dimension2D = new Dimension2D(32 * sizePerPercent, yProportional);
        }
        graphicsContext.fillText(symbol + "", dimension2D.getWidth(), dimension2D.getHeight());
    }

    /**
     * initialize the confirm button
     * @param createConversationConfirmButton confirm button in the add conversation mode
     */
    public void initConfirmBtn(WebView createConversationConfirmButton) {
        createConversationConfirmButton.getEngine().setUserStyleSheetLocation(svghelper.getStyleConfirm());
        createConversationConfirmButton.getEngine().loadContent(svghelper.getHTMLImage(SimpleSVGHTMLClass.IMAGE.CONFIRMBTN), "text/html");
    }
}
