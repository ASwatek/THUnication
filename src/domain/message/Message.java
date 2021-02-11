package domain.message;

import domain.user.User;

import java.io.Serializable;

public interface Message extends Serializable {

    /**
     * @return content of the message
     */
    Object getContent();

    /**
     * @return identification of the message
     */
    int getId();

    /**
     * @return author of the message
     */
    User getAuthor();

    /**
     * @return timestamp of the message
     */
    long getTimestamp();

    /**
     * @param content set the message content
     */
    void setContent(Object content);
}
