package network;

import java.io.Serializable;
import java.util.Set;

public class Permissions implements Serializable {

    private Set<Integer> userIds;
    private int conversationID;

    public Permissions(Set<Integer> userIds, int conversationID) {
        this.userIds = userIds;
        this.conversationID = conversationID;
    }

    public Set<Integer> getUserIds() {
        return userIds;
    }

    public int getConversationID() {
        return conversationID;
    }
}
