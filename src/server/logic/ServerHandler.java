package server.logic;

import domain.Conversation;
import domain.message.Message;
import domain.user.User;
import network.*;
import server.database.Database;
import server.logic.parser.MessageParser;
import server.network.ServerNetwork;

import java.util.List;

/**
 * Handles the business logic on server side.
 */
public class ServerHandler implements NetworkPackageHandler {

    private ServerNetwork network;
    private Database database;

    public ServerHandler(ServerNetwork serverNetwork) {
        this.network = serverNetwork;
        this.database = new Database();
    }

    /**
     * Handles incoming packages from clients.
     *
     * @param networkPackage
     */
    @Override
    public void handlePackage(NetworkPackage networkPackage) {
        switch (networkPackage.getType()) {
            case MESSAGE:
                this.handleMessage(networkPackage);
                break;

            case MESSAGES:
                this.handleMessages(networkPackage);
                break;

            case CONVERSATIONS:
                this.handleConversations(networkPackage);
                break;

            case REGISTER:
                this.handleRegister(networkPackage);
                break;

            case LOGIN:
                this.handleLogin(networkPackage);
                break;

            case USERS:
                this.handleUsers(networkPackage);
                break;

            case CONVERSATION:
                this.handleCreatingConversation(networkPackage);
                break;

            case PERMISSIONS:
                this.handlePermissions(networkPackage);
                break;

            default:
                throw new UnsupportedOperationException("Type of network package is unsupported.");
        }
    }

    private void handleRegister(NetworkPackage networkPackage) {
        RegisterData registerData = (RegisterData) networkPackage.getContent();

        String username = registerData.getUsername();
        String password = registerData.getPassword();
        String role = registerData.getRole();

        System.out.println("*Register*Username: " + username + " Password: " + password);

        if (!database.isAvailableUsername(username)) {
            network.sendPackage(new NetworkPackage(NetworkPackage.Type.REGISTER, null, "Username not available."), networkPackage.getSourceId());
            return;
        }

        database.createUser(username, password, role);
        User user = database.login(username, password);

        if (user == null) {
            // error
            network.sendPackage(new NetworkPackage(NetworkPackage.Type.REGISTER, null, "Register not successful"), networkPackage.getSourceId());
        } else {
            network.addUserToAuthenticatedClients(user, networkPackage.getSourceId());
            network.sendPackage(new NetworkPackage(NetworkPackage.Type.REGISTER, user), networkPackage.getSourceId());
        }
    }

    private void handleLogin(NetworkPackage networkPackage) {
        LoginCredentials loginCredentials = (LoginCredentials) networkPackage.getContent();

        String username = loginCredentials.getUsername();
        String password = loginCredentials.getPassword();

        System.out.println("*Login*Username: " + username + " Password: " + password);

        User user = database.login(username, password);

        if (user == null) {
            // Login was not successful.
            network.sendPackage(new NetworkPackage(NetworkPackage.Type.LOGIN, null, "Login credentials are invalid."), networkPackage.getSourceId());
        } else {
            // Login credentials were correct, but user could already be logged in on another device.
            boolean firstLogin = network.addUserToAuthenticatedClients(user, networkPackage.getSourceId());
            if (firstLogin) {
                network.sendPackage(new NetworkPackage(NetworkPackage.Type.LOGIN, user), networkPackage.getSourceId());
            } else {
                // Reject login as the underlying network implementation does not support multiple clients for one user.
                network.sendPackage(new NetworkPackage(NetworkPackage.Type.LOGIN, null, "You are already logged in on another device."), networkPackage.getSourceId());
            }
        }
    }

    private void handleMessage(NetworkPackage networkPackage) {
        Message message = (Message) networkPackage.getContent();
        int conversationId = (int) networkPackage.getAdditionalData();

        // Check if author is allowed to write messages.
        if (!database.canWrite(conversationId, message.getAuthor().getId())) {
            network.sendPackage(new NetworkPackage(NetworkPackage.Type.MESSAGE, null, "You don't have the permission to write messages to this conversation.", networkPackage.getAdditionalData()), new User[]{message.getAuthor()});
            return;
        }

        message.setContent(MessageParser.parse((String) message.getContent()));

        // We need to use the returned message object otherwise the message id will be missing.
        message = database.addMessage(conversationId, message);

        // Forward message to participants.
        List<User> participants = database.getParticipants(conversationId);
        NetworkPackage np = new NetworkPackage(NetworkPackage.Type.MESSAGE, message);
        np.setAdditionalData(conversationId);

        User[] participantsArr = new User[participants.size()];
        participantsArr = participants.toArray(participantsArr);
        network.sendPackage(np, participantsArr);
    }

    private void handleMessages(NetworkPackage networkPackage) {
        List<Message> messageList = database.getMessages((int) networkPackage.getAdditionalData(), 30);
        Message[] messages = new Message[messageList.size()];
        messages = messageList.toArray(messages);

        // Send the last 30 messages to client. The latest message is at index 0.
        NetworkPackage np = new NetworkPackage(NetworkPackage.Type.MESSAGES, messages);
        np.setAdditionalData((int) networkPackage.getAdditionalData());
        network.sendPackage(np, networkPackage.getSourceId());
    }

    private void handleConversations(NetworkPackage networkPackage) {
        List<Conversation> conversationList = database.getConversations((int) networkPackage.getAdditionalData());
        Conversation[] conversations = new Conversation[conversationList.size()];
        conversations = conversationList.toArray(conversations);
        network.sendPackage(new NetworkPackage(NetworkPackage.Type.CONVERSATIONS, conversations), networkPackage.getSourceId());
    }

    private void handleUsers(NetworkPackage networkPackage) {
        List<User> userList = database.getRegisteredUsers();
        User[] users = new User[userList.size()];
        users = userList.toArray(users);
        network.sendPackage(new NetworkPackage(NetworkPackage.Type.USERS, users), networkPackage.getSourceId());
    }

    private void handleCreatingConversation(NetworkPackage networkPackage) {
        Conversation conversation = (Conversation) networkPackage.getContent();
        conversation = database.createConversation(conversation);

        // Heads up: New conversation has to be send to all participants and not only to the person who created it.
        User[] users = new User[conversation.getParticipants().size()];
        users = conversation.getParticipants().toArray(users);
        network.sendPackage(new NetworkPackage(NetworkPackage.Type.CONVERSATION, conversation), users);
    }

    private void handlePermissions(NetworkPackage networkPackage) {
        Permissions permissions = (Permissions) networkPackage.getContent();
        permissions.getUserIds().forEach(System.out::println);
        database.updatePermissions(permissions.getConversationID(), permissions.getUserIds());

        // Propagate permission changes to all participants.
        List<User> participantList = database.getParticipants(permissions.getConversationID());
        User[] participants = new User[participantList.size()];
        participants = participantList.toArray(participants);
        network.sendPackage(new NetworkPackage(NetworkPackage.Type.PERMISSIONS, permissions), participants);
    }

    @Override
    public void networkConnectionLost() {
        // Handling of removing lost connection data is already performed in the network layer itself.
    }
}
