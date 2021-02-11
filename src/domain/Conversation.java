package domain;

import domain.message.Message;
import domain.user.User;

import java.io.Serializable;
import java.util.*;

public class Conversation implements Serializable {

    private boolean isGroupChat;
    private List<Message> messages = new LinkedList<>();
    private LinkedList<User> participants = new LinkedList<>();
    private User owner;
    private int id;
    private String title;
    private long lastChangeTime;
    private boolean messagesLoaded = false;

    /**
     * Contains the user ids of all users who have the 'write' permission for this conversation.
     */
    private Set<Integer> canWritePermissions;

    public Conversation(User owner, boolean isGroupChat, int conversationId, String title, long lastChangeTime) {
        this.owner = owner;
        this.isGroupChat = isGroupChat;
        this.id = conversationId;
        this.title = title;
        this.lastChangeTime = lastChangeTime;
    }

    /**
     * @param canWritePermissions set write permission each participants in a group chat
     */
    public void setCanWritePermissions(Set<Integer> canWritePermissions) {
        this.canWritePermissions = canWritePermissions;
    }

    public Set<Integer> getCanWritePermissions() {
        return canWritePermissions;
    }

    /**
     * @param title will be shown in the UI
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @param message new message for the conversation
     */
    public void addNewMessage(Message message) {
        this.messages.add(0, message);
    }

    public void addNewMessages(Message[] messages) {
        this.messages.addAll(0, Arrays.asList(messages));
    }

    /**
     * Used for adding old messages to the message list.
     *
     * @param messages
     */
    public void addOldMessages(Message[] messages) {
        this.messages.addAll(Arrays.asList(messages));
    }

    /**
     * @return list of messages, the latest message is at index `0`.
     */
    public List<Message> getMessageList() {
        return this.messages;
    }

    public void changeOwner(User user) {
        this.owner = user;
    }

    public void addParticipant(User user) {
        if (isGroupChat || getParticipants().size() == 0)
            this.participants.add(user);
    }

    /**
     * @param participants participants of the conversation
     */
    public void setParticipants(User[] participants) {
        this.participants.addAll(Arrays.asList(participants));
    }

    /**
     * @param participants add more participants into the list
     */
    public void addParticipants(List<User> participants) {
        this.participants.addAll(participants);
    }

    /**
     * @param messageList replace the actual list of messages
     */
    public void setMessages(List<Message> messageList) {
        this.messages = messageList;
    }

    /**
     * @return list of messages, the latest message is at index `0`.
     */
    public Message[] getMessages() {
        Message[] messages = new Message[this.messages.size()];
        return this.messages.toArray(messages);
    }

    /**
     * @param user which get kicked from the group
     */
    public void removeParticipant(User user) {
        if (isGroupChat)
            this.participants.remove(user);
    }

    /**
     * @return list of all participants
     */
    public List<User> getParticipants() {
        return participants;
    }

    /**
     * @return private chat or group chat status
     */
    public boolean isGroupChat() {
        return isGroupChat;
    }

    /**
     * @return give the number of messages in this conversation
     */
    int getCountMessages() {
        return messages.size();
    }

    /**
     * @return owner of the conversation
     */
    public User getOwner() {
        return owner;
    }

    /**
     * @return id of conversation
     */
    public int getId() {
        return id;
    }

    /**
     * @return title of conversation
     */
    public String getTitle() {
        return title;
    }

    /**
     * Compared by id
     * @param object other object which has to be compared
     * @return a boolean value if they are equal
     */
    @Override
    public boolean equals(Object object) {
        if (object == null || object.getClass() != getClass())
            return false;
        Conversation conv = (Conversation) object;
        return id == conv.id;
    }

    /**
     * @return for quick finding the conversation in a list
     */
    @Override
    public int hashCode() {
        return Integer.MAX_VALUE - id;
    }

    /**
     * @return lastChange timestamp
     */
    public long getLastChangeTime() {
        return lastChangeTime;
    }

    /**
     * @return true if the messages were fetched from server
     */
    public boolean messagesLoaded() {
        return messagesLoaded;
    }

    public void setMessagesLoaded(boolean messagesLoaded) {
        this.messagesLoaded = messagesLoaded;
    }
}
