package network;

import java.io.Serializable;

/**
 * Every object which needs to be transmitted over the network will be wrapped by
 * this class.
 */
public class NetworkPackage implements Serializable {

    private Type type;
    private Object content;
    private Object additionalData;
    private String errorMessage = "";
    private int sourceId;

    public NetworkPackage(Type type, Object content) {
        this.type = type;
        this.content = content;
    }

    public NetworkPackage(Type type, Object content, String errorMessage) {
        this.type = type;
        this.content = content;
        this.errorMessage = errorMessage;
    }

    public NetworkPackage(Type type, Object content, String errorMessage, Object additionalData) {
        this.type = type;
        this.content = content;
        this.errorMessage = errorMessage;
        this.additionalData = additionalData;
    }

    /**
     * Allows to send additional data to the receiver.
     *
     * @param additionalData anything which can be useful
     */
    public void setAdditionalData(Object additionalData) {
        this.additionalData = additionalData;
    }

    public Type getType() {
        return type;
    }

    public Object getContent() {
        return content;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Object getAdditionalData() {
        return additionalData;
    }

    public boolean hasValidationError() {
        return !"".equals(this.errorMessage);
    }

    /**
     * Returns the unique identifier of the sender of this package. Can be null if package comes from server.
     *
     * @return identifier of client or null if package comes from server
     */
    public int getSourceId() {
        return sourceId;
    }

    /**
     * Sets the unique identifier of the sender of this package. Can be null if package comes from server.
     */
    public void setSourceId(int sourceId) {
        this.sourceId = sourceId;
    }

    /**
     * Defines the type of the package. This way the recipient knows what the sender
     * wants.
     */
    public enum Type {
        REGISTER,
        LOGIN,
        MESSAGE, // a message is transferred
        MESSAGES, // a list of messages is transferred, mainly used for initialization
        CONVERSATIONS, // a list of conversations is transferred
        CONVERSATION, // a single conversation is transferred with participants - used for creating a conversation
        USERS, // a list of users is transferred
        PERMISSIONS,
    }
}
