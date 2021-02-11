package client.logic;

import domain.Conversation;
import domain.user.User;

import java.util.Collection;
import java.util.HashMap;

public class ApplicationModel {

    private HashMap<Integer, Conversation> conversations = new HashMap<>();
    private User currentUser;
    private int actualConversationID = 0;

    /**
     * @param conversationId identification of the conversation
     * @return the conversation with a equal Id
     */
    public Conversation getConversation(int conversationId) {
        return conversations.get(conversationId);
    }

    /**
     * @return list of all existing Conversation
     */
    public Collection<Conversation> getConversationAsList() {
        return conversations.values();
    }

    /**
     * @return current user which is logged in this session
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * @param conversation the new conversation which should be added in the model
     */
    public void addConversation(Conversation conversation) {
        if (!conversation.isGroupChat()) {
            for (User user : conversation.getParticipants()) {
                if (!user.equals(currentUser))
                    conversation.setTitle(user.getUsername());
            }
        }
        conversations.put(conversation.getId(), conversation);
    }

    /**
     * @param user The user which is logged in this session
     */
    public void setCurrentUser(User user) {
        currentUser = user;
    }

    /**
     * This method is only useful for private chats. It helps to find out for a existing conversation.
     * @param user user != current user
     * @return null or a conversation -> non-exist or exist
     */
    public Conversation existConv(User user) {
        for (Conversation conv : getConversationAsList()) {
            if (!conv.isGroupChat()) {
                for (User users : conv.getParticipants()) {
                    if (users.equals(user))
                        return conv;
                }
            }
        }
        return null;
    }

    /**
     * @return of actual conversationID
     */
    public int getActualConversationID() {
        return actualConversationID;
    }

    /**
     * @param actualConversationID set the new actual conversationID
     */
    public void setActualConversationID(int actualConversationID) {
        this.actualConversationID = actualConversationID;
    }
}
