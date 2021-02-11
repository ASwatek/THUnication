package domain.message;

import domain.user.User;

public class TextMessage implements Message {

    private int id;
    private User author;
    private String content;
    private long timestamp;

    public TextMessage(int id, User author, String content, long timestamp) {
        this.id = id;
        this.author = author;
        this.content = content;
        this.timestamp = timestamp;
    }

    /**
     * @return identification of this message
     */
    public int getId() {
        return id;
    }

    /**
     * @return author of this message
     */
    public User getAuthor() {
        return author;
    }

    /**
     * @return timestamp of this message
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * @return content as text
     */
    @Override
    public String getContent() {
        return content;
    }

    /**
     * @param content must be STRING!
     */
    @Override
    public void setContent(Object content) {
        this.content = (String) content;
    }
}
