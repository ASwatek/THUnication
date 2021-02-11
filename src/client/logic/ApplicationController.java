package client.logic;

import client.THUnicationApp;
import client.network.ClientNetwork;
import client.ui.ApplicationView;
import domain.Conversation;
import domain.message.Message;
import domain.message.TextMessage;
import domain.user.User;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Font;
import javafx.scene.web.WebView;
import network.*;

import java.net.URL;
import java.util.*;


public class ApplicationController implements Initializable, NetworkPackageHandler {

    private boolean ctrlKeyPressed;
    private ApplicationView view;
    private ApplicationModel model;
    private ClientNetwork network;

    @FXML
    private Canvas avatarConv;

    @FXML
    private Canvas avatarImg;

    @FXML
    private TextField groupnameText;

    @FXML
    private Label currentUserText;

    @FXML
    private CheckBox groupCheckBox;

    @FXML
    private Label conversationTitle;

    @FXML
    private WebView chatWebView;

    @FXML
    private TextArea textMessage;

    @FXML
    private ListView<Conversation> conversationListView;

    @FXML
    private ListView<User> selectParticipantsListView;

    @FXML
    private WebView showConversationListButton;

    @FXML
    private WebView showCreateConversationButton;

    @FXML
    private WebView createConversationConfirmButton;

    @FXML
    private WebView editWritePermissionButton;

    @FXML
    private WebView showParticipantsButton;

    /**
     * This method will be triggered after UI is created.
     * Network, view and model has to be set and the app has to be initialize.
     * @param url predefined but not needed -> you can find out which fxml file will be executed
     * @param resourceBundle predefined but not needed
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        network = THUnicationApp.getClientNetwork();
        view = THUnicationApp.getApplicationView();
        model = THUnicationApp.getModel();
        if (network != null) {
            network.setPackageHandler(this);
        }
        Platform.runLater(this::appIsStartedInit);
    }

    @Override
    public void networkConnectionLost() {
        Platform.runLater(() -> view.showNoServerConnection());
    }

    /**
     * This method handle server packages.
     * @param networkPackage network data
     */
    @Override
    public void handlePackage(NetworkPackage networkPackage) {
        switch (networkPackage.getType()) {
            case MESSAGE: {
                int conversationId = (int) networkPackage.getAdditionalData();
                Message message = (Message) networkPackage.getContent();

                // In case a conversations gets a new message but the last messages are not already loaded (they are only
                // loaded when the user has opened the conversation already) the response of the MESSAGE event is discarded
                // and a 'normal' request for loading all last messages is send to the server.
                if (!model.getConversation(conversationId).messagesLoaded()) {
                    network.sendPackage(new NetworkPackage(NetworkPackage.Type.MESSAGES, null, null, conversationId));
                    Platform.runLater(() -> view.convToTheTop(model.getConversation(conversationId)));
                    return;
                }

                if (message == null) {
                    // Author is not allowed to write messages to this conversation.
                    Platform.runLater(() -> view.showErrorMessage("Message rejected", "Message rejected", "You don't have the permission to write messages."));
                } else {
                    this.receiveNewMessage(conversationId, message);
                }
                break;
            }

            case MESSAGES: {
                int conversationId = (int) networkPackage.getAdditionalData();
                model.getConversation(conversationId).setMessagesLoaded(true);
                Message[] messages = (Message[]) networkPackage.getContent();
                this.receiveNewMessage(conversationId, messages);
                break;
            }

            case CONVERSATIONS:
                Conversation[] conversations = (Conversation[]) networkPackage.getContent();
                // New conversations are always inserted at position 0, therefore we have to
                // loop in the opposite direction on initialization.
                for (int i = conversations.length - 1; i >= 0; i--) {
                    this.receiveNewConversation(conversations[i]);
                }
                if (conversations.length > 0) {
                    model.setActualConversationID(conversations[0].getId());
                    this.updateCurrentConversationUi();
                    Platform.runLater(() -> {
                        conversationTitle.setText(conversations[0].getTitle());
                        view.drawAvatar(avatarConv, conversations[0].getTitle());

                        // Mark first conversation *visually* as active.
                        if (!conversationListView.getItems().isEmpty()) {
                            conversationListView.getSelectionModel().select(0);
                        }
                    });
                }
                break;

            case USERS:
                if (networkPackage.hasValidationError()) return;

                User[] users = (User[]) networkPackage.getContent();

                Platform.runLater(() -> view.initAddConvListView(selectParticipantsListView, users));
                break;

            case CONVERSATION:
                if (networkPackage.hasValidationError()) {
                    System.out.println("unable to create new conversation");
                    return;
                }

                Conversation conversation = (Conversation) networkPackage.getContent();

                receiveNewConversation(conversation);

                if (conversation.getOwner().equals(model.getCurrentUser())) {
                    // Switch to new and empty conversation only for the owner of the conv
                    Platform.runLater(() -> {
                        try {
                            toggleLeftSidebarContent();
                            this.switchConversation(conversation);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
                break;

            case PERMISSIONS:
                Permissions permissions = (Permissions) networkPackage.getContent();
                model.getConversation(permissions.getConversationID()).setCanWritePermissions(permissions.getUserIds());
                this.updateCurrentConversationUi();
                break;

            default:
                throw new UnsupportedOperationException("Type of network package is unsupported.");
        }
    }

    /**
     * This method looks for pressing the control button
     * @param event which key is pressed
     */
    @FXML
    public void keyHandlerPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.CONTROL) {
            ctrlKeyPressed = true;
        }
    }

    /**
     * ctrl + enter (event trigger) -> new Message
     * event -> ctrl -> update boolean variable
     * new Message is not allowed to be longer than 500 chars or blank
     * @param event which key is released
     */
    @FXML
    public void keyHandlerRelease(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER && ctrlKeyPressed) {
            String enteredText = textMessage.getText().trim();

            if (enteredText.isBlank()) {
                return;
            } else if (enteredText.length() > 500) {
                view.showErrorMessage("Too much characters!", "Message is too long.", "You cannot send more than 500 characters per message!");
                return;
            }

            Message message = new TextMessage(0, model.getCurrentUser(), enteredText, (new Date(System.currentTimeMillis())).getTime());
            textMessage.clear();

            NetworkPackage networkPackage = new NetworkPackage(NetworkPackage.Type.MESSAGE, message);
            networkPackage.setAdditionalData(model.getActualConversationID());
            network.sendPackage(networkPackage);
        } else if (event.getCode() == KeyCode.CONTROL) {
            ctrlKeyPressed = false;
        }
    }

    /**
     * This method triggers the addConversation mode
     */
    @FXML
    public void clickOnAddConvBtn() {
        // Fetch registered users from server.
        network.sendPackage(new NetworkPackage(NetworkPackage.Type.USERS, null));
        toggleLeftSidebarContent();
    }

    /**
     * This method triggers back to the conversation mode
     */
    @FXML
    public void clickOnConv() {
        toggleLeftSidebarContent();
    }

    /**
     * This method tries to create a new conversation.
     * If the conversation is private and exists already, then it will not create a new conversation.
     * In this case it will change to the conversation mode and activate this existing conversation.
     * Group name is not allowed to be blank. A private chat is not allowed to have more than one participant.
     */
    @FXML
    public void clickOnAddConvConfirmBnt() {
        // For creating a group the server expects a conversation object which
        // contains the list of participants.
        Conversation conversation;

        if (!groupCheckBox.isSelected() && selectParticipantsListView.getSelectionModel().getSelectedItems().size() > 1) {
            view.showErrorMessage("Create Conversation Error", "Too many participants selected", "In a direct conversation only one participant is allowed.");
            return;
        }

        if (groupCheckBox.isSelected() && groupnameText.getText().isBlank()) {
            view.showErrorMessage("Create Conversation Error", "Group name empty.", "Please enter a name for your group.");
            return;
        }

        conversation = new Conversation(
                model.getCurrentUser(),
                groupCheckBox.isSelected(),
                0,
                groupCheckBox.isSelected() ? groupnameText.getText() : "direct chat created by " + model.getCurrentUser().getUsername(),
                new Date(System.currentTimeMillis()).getTime()
        );

        // Add selected users as participants to the conversation.
        selectParticipantsListView.getSelectionModel().getSelectedItems().forEach(conversation::addParticipant);
        if (!groupCheckBox.isSelected())
            if (!(conversation.getParticipants().size() == 1)) {
                return;
            } else {
                Conversation conv = model.existConv(conversation.getParticipants().get(0));
                if (conv != null) {
                    toggleLeftSidebarContent();
                    switchConversation(conv);
                    return;
                }
            }
        network.sendPackage(new NetworkPackage(NetworkPackage.Type.CONVERSATION, conversation));

        groupnameText.clear();
        groupCheckBox.setSelected(false);
        selectParticipantsListView.getSelectionModel().clearSelection();
    }

    /**
     * Called when the `edit conversation` button is pressed.
     */
    @FXML
    public void buttonEditConversationClicked() {

        Optional<Set<Integer>> result = view.createEditDialog();

        result.ifPresent(userIds -> {
            model.getConversation(model.getActualConversationID()).setCanWritePermissions(userIds);
            network.sendPackage(new NetworkPackage(NetworkPackage.Type.PERMISSIONS, new Permissions(userIds, model.getActualConversationID())));
        });
    }

    /**
     * Called when the 'participant' button is pressed.
     * The button is only available when the conversation is a group chat!
     */
    @FXML
    public void showParticipantsBtnClicked() {
        view.createShowParticipantDialog();
    }

    /**
     * Method to init the application after the UI is created.
     * Cannot done before because otherwise it will trigger a NullPointerException!
     */
    private void appIsStartedInit() {
        conversationListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Conversation item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.getTitle() == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Canvas canvas = new Canvas(35, 35);
                    view.drawAvatar(canvas, item.getTitle());
                    setGraphic(canvas);
                    setFont(Font.font("Calibri", 17));
                    setText(item.getTitle());

                    setOnMouseClicked(touchEvent -> {
                        // Switch Conversation

                        if (item.getId() != model.getActualConversationID())
                            switchConversation(item);
                    });
                }
            }
        });
        Label placeholder = new Label("You don't have any conversations");
        placeholder.setFont(Font.font("Calibri", 20.0));
        conversationListView.widthProperty().addListener((observableValue, oldValue, newValue) -> {
            if (newValue.doubleValue() > 415)
                placeholder.setFont(Font.font("Calibri", newValue.doubleValue() / 26.0));
            else
                placeholder.setFont(Font.font("Calibri", newValue.doubleValue() / 18.0));
        });
        conversationListView.setPlaceholder(placeholder);

        view.initAppWebViews(chatWebView, conversationListView, showConversationListButton, showCreateConversationButton, editWritePermissionButton, showParticipantsButton);
        view.initConfirmBtn(createConversationConfirmButton);
        view.drawAvatar(avatarImg, model.getCurrentUser().getUsername());

        currentUserText.setText(model.getCurrentUser().getUsername());

        // Textfield for entering a group name should only be available if the corresponding checkbox is checked.
        groupCheckBox.selectedProperty().addListener((observableValue, aBoolean, t1) -> groupnameText.setDisable(!t1));
        
        textMessage.disabledProperty().addListener((observableValue, aBoolean, t1) -> {
            if (observableValue.getValue())
                textMessage.setText("You don't have permission to write messages to this conversation.");
            else
                textMessage.clear();
        });

        // App is started so load conversations.
        NetworkPackage np1 = new NetworkPackage(NetworkPackage.Type.CONVERSATIONS, null);
        np1.setAdditionalData(model.getCurrentUser().getId());
        network.sendPackage(np1);
    }

    /**
     * Toggle between conversation mode and addConversation mode
     */
    private void toggleLeftSidebarContent() {
        boolean newState = false;
        if (conversationListView.isVisible()) {
            // Show now the conversation add interface.
            newState = true;
        }

        conversationListView.setVisible(!newState);
        selectParticipantsListView.setVisible(newState);
        createConversationConfirmButton.setVisible(newState);
        groupCheckBox.setVisible(newState);
        groupnameText.setVisible(newState);

        view.toggleButtonState(showConversationListButton, showCreateConversationButton);
    }

    /**
     * This method add a new Conversation into the model
     * @param conversation New conversation which is received by the server.
     */
    private void receiveNewConversation(Conversation conversation) {
        model.addConversation(conversation);
        Platform.runLater(() -> {
            view.addNewConv(conversation);
            if (!conversation.getMessageList().isEmpty()) {
                view.addMessages(conversation.getMessages());
                view.updateApp(chatWebView);
            }
            view.convToTheTop(conversation);
        });
    }

    /**
     * This method add a new message in a existing conversation
     * @param convId ConversationID is needed to know which of these conversation get the new message
     * @param message new message which need to be added
     */
    private void receiveNewMessage(int convId, Message message) {
            Platform.runLater(() -> {
                view.convToTheTop(model.getConversation(convId));
                model.getConversation(convId).addNewMessage(message);
                this.updateCurrentConversationUi();

                if (model.getActualConversationID() == convId) {
                    view.addNewMessage(message);
                    view.updateApp(chatWebView);
                }
            });
    }

    /**
     * This method can add more than one new messages in a existing conversation
     * @param convId ConversationID is needed to know which of these conversation get the new message
     * @param messages one or more message which need to be added
     */
    private void receiveNewMessage(int convId, Message[] messages) {
        if (messages.length == 0)
            return;
        model.getConversation(convId).addNewMessages(messages);
        if (model.getActualConversationID() == convId) {
            Platform.runLater(() -> {
                view.addMessages(messages);
                view.updateApp(chatWebView);
            });
        }
    }

    /**
     * This method loads content of this existing conversation in the model and set it to a actual conversation
     * @param newActualConversation Existing conversation in the model which get activated
     */
    private void switchConversation(Conversation newActualConversation) {
        model.setActualConversationID(newActualConversation.getId());
        conversationTitle.setText(newActualConversation.getTitle());
        view.drawAvatar(avatarConv, newActualConversation.getTitle());
        if (newActualConversation.getMessageList().isEmpty()) {
            System.out.println("Switching conversation to " + newActualConversation.getTitle() + "; message list empty, fetch from server...");
            // Message list is empty, fetch it from server.
            NetworkPackage np = new NetworkPackage(NetworkPackage.Type.MESSAGES, null);
            np.setAdditionalData(newActualConversation.getId());
            network.sendPackage(np);
        }
        view.loadNewConversation(newActualConversation, chatWebView);

        this.updateCurrentConversationUi();
    }

    /**
     * Updates the visibility of the conversation option buttons according to the current
     * conversation in the model.
     */
    private void updateCurrentConversationUi() {
        // Group permission button is only available for the group owner.
        Conversation conversation = model.getConversation(model.getActualConversationID());
        editWritePermissionButton.setVisible(conversation.isGroupChat() && conversation.getOwner().equals(model.getCurrentUser()));
        showParticipantsButton.setVisible(conversation.isGroupChat());

        // Mark current conversation in model as active.
        conversationListView.getSelectionModel().clearSelection();
        conversationListView.getSelectionModel().select(model.getConversation(model.getActualConversationID()));

        // Update disabled state of message input according to the permission rights.
        textMessage.setDisable(!model.getConversation(model.getActualConversationID()).getCanWritePermissions().contains(model.getCurrentUser().getId()));
    }

}