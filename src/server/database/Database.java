package server.database;

import domain.Conversation;
import domain.message.Message;
import domain.message.TextMessage;
import domain.user.Student;
import domain.user.Teacher;
import domain.user.User;

import java.sql.*;
import java.util.*;

public class Database {
    protected Connection connection = null;
    private final static String URL = "jdbc:sqlite:thunication.db";
    //private final static String URL = "jdbc:sqlite::resource:thunication.db";

    protected Connection getConnection() {
        if (connection != null) {
            return connection;
        }
        try {
            connect();
        } catch (SQLException e) {
            System.out.println("Connection Failed");

        }
        return connection;
    }

    protected void connect() throws SQLException {
        System.out.println("Attempting to connect to THUnication");

        connection = DriverManager.getConnection(URL);

        System.out.println(connection);
    }

    private void closeConnection() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns true if the given username is not in use.
     *
     * @param username
     * @return true if username is not in use
     */
    public boolean isAvailableUsername(String username) {
        try {
            String sql = "select COUNT(*) from user WHERE username = ?";
            PreparedStatement statement = getConnection().prepareStatement(sql);

            statement.setString(1, username);

            ResultSet rs = statement.executeQuery();
            return rs.getInt(1) == 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Creates a new user in database.
     *
     * @param username
     * @param password
     * @param role     either `Student` or `Teacher`
     */
    public void createUser(String username, String password, String role) {
        try {
            String sql = "INSERT INTO user(Username, Password, Role) VALUES(?, ?, ?)";
            PreparedStatement statement = getConnection().prepareStatement(sql);

            statement.setString(1, username);
            statement.setString(2, password);
            statement.setString(3, role);

            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * If the provided login credentials are invalid, null is returned
     * otherwise the associated user object will be returned.
     *
     * @param username username
     * @param password password for given username
     * @return user object of logged in user or null
     */
    public User login(String username, String password) {   //TODO: salting/hashing password
        try {
            String sql = "SELECT * from user WHERE username = ?";
            PreparedStatement statement = getConnection().prepareStatement(sql);
            statement.setString(1, username);
            ResultSet rs = statement.executeQuery();

            if (!rs.next()) {
                return null;
            }

            if (!password.equals(rs.getString("password"))) {
                return null;
            }

            if (rs.getString("Role").equals("Teacher")) {
                return new Teacher(
                        rs.getString("username"),
                        rs.getInt("userID")
                );
            } else {
                return new Student(
                        rs.getString("username"),
                        rs.getInt("userID")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Creates a new conversation in the database.
     *
     * @param conversation conversation object which contains a list of participants.
     */
    public Conversation createConversation(Conversation conversation) {
        // Add owner of group to the list of participants so that he can be easier added to
        // the participantOf table.
        if (!conversation.getParticipants().contains(conversation.getOwner())) {
            conversation.getParticipants().add(conversation.getOwner());
        }

        Conversation oldConversation = conversation;

        try {
            // Create entry in conversation table.
            String sql = "INSERT INTO conversation(timestamp, title, createrID, isGroup) VALUES (?, ? , ?,?)";
            PreparedStatement statement = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, conversation.getLastChangeTime());
            statement.setString(2, conversation.getTitle());
            statement.setInt(3, conversation.getOwner().getId());
            statement.setInt(4, (conversation.isGroupChat() ? 1 : 0));
            statement.execute();

            // Try to fetch the id of the inserted conversation.
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    conversation = new Conversation(
                            conversation.getOwner(),
                            conversation.isGroupChat(),
                            generatedKeys.getInt(1),
                            conversation.getTitle(),
                            conversation.getLastChangeTime()
                    );
                } else {
                    throw new SQLException("Unable to obtain conversation id of inserted conversation");
                }
            }

            // Create entries in paricipantOf table.
            for (User user : oldConversation.getParticipants()) {
                sql = "INSERT INTO ParticipantOf(userID, JoinDate, conversationID) VALUES (?, ?, ?)";
                PreparedStatement statement2 = getConnection().prepareStatement(sql);
                statement2.setLong(2, conversation.getLastChangeTime());
                statement2.setInt(3, conversation.getId());
                statement2.setInt(1, user.getId());
                statement2.execute();
            }
            conversation.addParticipants(oldConversation.getParticipants());
            Set<Integer> permissions = new HashSet<>();
            conversation.getParticipants().forEach(user -> permissions.add(user.getId()));
            conversation.setCanWritePermissions(permissions);

            return conversation;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Stores a new message for the given conversation id in the database.
     *
     * @param conversationId id of the conversation the message belongs to
     * @param message        new message
     */
    public Message addMessage(int conversationId, Message message) {
        try {
            updateLastChange(conversationId, message.getTimestamp());
            String sql = "INSERT INTO message(authorID, content, timestamp, conversationID) VALUES(?, ?, ?, ?)";
            PreparedStatement statement = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            statement.setInt(1, message.getAuthor().getId());

            String original = (String) message.getContent();
            //String conv = emojiConversion(original);
            statement.setString(2, original);

            statement.setLong(3, message.getTimestamp());
            statement.setInt(4, conversationId);

            statement.executeUpdate();

            // Fetch the id of the inserted message.
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    if (message instanceof TextMessage) {
                        message = new TextMessage(generatedKeys.getInt(1), message.getAuthor(), ((TextMessage) message).getContent(), message.getTimestamp());
                    }
                } else {
                    throw new SQLException("Unable to obtain conversation id of inserted conversation");
                }
            }

            return message;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Returns the last `n` messages, starting at the specified `offset` position, of the conversation with the given id.
     *
     * @param conversationId id of the conversation for which the messages should be loaded
     * @param n              number of messages which should be returned at maximum
     * @param offset
     * @return last `n` messages
     */
    public List<Message> getMessages(int conversationId, int n, int offset) {
        try {
            String sql = "SELECT * FROM Message LEFT JOIN user ON message.authorID = user.userID WHERE conversationID = ? ORDER BY messageID DESC LIMIT ? OFFSET ?";
            PreparedStatement statement = getConnection().prepareStatement(sql);
            statement.setInt(1, conversationId);
            statement.setInt(2, n);
            statement.setInt(3, offset);
            ResultSet rs = statement.executeQuery();

            ArrayList<Message> results = new ArrayList<>();
            while (rs.next()) {
                User author = null;
                if (rs.getString("Role").equals("Teacher"))
                    author = new Teacher(rs.getString("Username"), rs.getInt("userID"));
                else author = new Student(rs.getString("Username"), rs.getInt("userID"));

                results.add(new TextMessage(
                        rs.getInt("messageID"),
                        author,
                        rs.getString("content"),
                        rs.getLong("timestamp")
                ));
            }
            return results;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Collections.emptyList();
    }

    public List<Message> getMessages(int conversationId, int n) {
        return this.getMessages(conversationId, n, 0);
    }

    /**
     * Returns a list of all participants of the conversation with the given id.
     *
     * @param conversationID
     * @return participants of the given conversation id
     */
    public List<User> getParticipants(int conversationID) {
        try {
            String sql = "SELECT u.* FROM user u LEFT JOIN ParticipantOf p ON u.userID = p.userID WHERE conversationID = ? ORDER BY u.Username";
            PreparedStatement statement = getConnection().prepareStatement(sql);
            statement.setInt(1, conversationID);
            ResultSet rs = statement.executeQuery();

            ArrayList<User> results = new ArrayList<>();
            while (rs.next()) {
                User user = null;
                if (rs.getString("Role").equals("Teacher"))
                    user = new Teacher(rs.getString("Username"), rs.getInt("userID"));
                else user = new Student(rs.getString("Username"), rs.getInt("userID"));

                results.add(user);
            }
            return results;

        } catch (SQLException e) {
            System.out.println("Error: Could not get participants from group# " + conversationID);
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    /**
     * Returns all conversations of the given user id.
     *
     * @return list of conversations where the user participates
     */
    public List<Conversation> getConversations(int userID) {
        try {
            String sql = "SELECT * FROM conversation c LEFT JOIN ParticipantOf p ON c.conversationID = p.conversationID LEFT JOIN user u ON u.userID = c.createrID WHERE p.userID = ? ORDER BY timestamp DESC";
            PreparedStatement statement = getConnection().prepareStatement(sql);
            statement.setInt(1, userID);
            ResultSet rs = statement.executeQuery();

            ArrayList<Conversation> results = new ArrayList<>();
            while (rs.next()) {
                User user;
                if (rs.getString("Role").equals("Teacher"))
                    user = new Teacher(rs.getString("Username"), rs.getInt("userID"));
                else user = new Student(rs.getString("Username"), rs.getInt("userID"));

                results.add(new Conversation(
                        user,
                        (rs.getInt("isGroup") != 0),
                        rs.getInt("conversationID"),
                        rs.getString("title"),
                        rs.getLong("timestamp")
                ));
            }

            // For the first conversation also the messages are transmitted.
            if (!results.isEmpty() && results.get(0) != null) {
                results.get(0).setMessages(this.getMessages(results.get(0).getId(), 30));
                results.get(0).setMessagesLoaded(true);
            }

            // Set for all conversations the participants and the permissions.
            results.forEach(conversation -> {
                conversation.addParticipants(this.getParticipants(conversation.getId()));
                conversation.setCanWritePermissions(this.getPermissionValues(conversation.getId()));
            });

            return results;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    /**
     * Returns a list of all registered users.
     *
     * @return list of all registered users
     */
    public List<User> getRegisteredUsers() {
        try {
            String sql = "SELECT * FROM User ORDER BY Username";
            PreparedStatement statement = getConnection().prepareStatement(sql);
            ResultSet rs = statement.executeQuery();

            ArrayList<User> results = new ArrayList<>();
            while (rs.next()) {
                User user;
                if (rs.getString("Role").equals("Teacher"))
                    user = new Teacher(rs.getString("Username"), rs.getInt("userID"));
                else user = new Student(rs.getString("Username"), rs.getInt("userID"));

                results.add(user);
            }

            return results;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    /**
     * update date of last changes in a conversation
     * @param conversationID the identification of conversation
     * @param timestamp timestamp of last change
     */
    public void updateLastChange(int conversationID, long timestamp) {
        try {
            //set all write permissions to false
            String sql = "UPDATE conversation SET timestamp = ? WHERE conversationID = ?";
            PreparedStatement statement = getConnection().prepareStatement(sql);
            statement.setLong(1, timestamp);
            statement.setInt(2, conversationID);
            statement.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Failed to updatePermissions()");
            e.printStackTrace();
        }
    }

    /**
     * Updates the permissions values for the given conversation id.
     *
     * @param conversationID id of the conversation
     * @param userIds        set of user ids which should get the permission to write messages
     */
    public void updatePermissions(int conversationID, Set<Integer> userIds) {
        //  1.) Set ALL active write permissions for the given conversationID to `0` (false)
        //  2.) Add for all user ids in `userIds` the permission to write messages
        try {
            //set all write permissions to false
            String sql = "UPDATE ParticipantOf SET canWrite = 0 WHERE conversationID = ?";
            PreparedStatement statement = getConnection().prepareStatement(sql);
            statement.setInt(1, conversationID);
            statement.executeUpdate();


            String sql2 = "UPDATE ParticipantOf SET canWrite = 1 WHERE conversationID = ? AND userID = ?";
            PreparedStatement stmt = getConnection().prepareStatement(sql2);
            stmt.setInt(1, conversationID);
            //set write permission for specific users to true
            for (Integer userID : userIds) {
                stmt.setInt(2, userID);
                stmt.executeUpdate();
            }

        } catch (SQLException e) {
            System.out.println("Failed to updatePermissions()");
            e.printStackTrace();
        }
    }

    /**
     * Fetches all user ids who have the permission to write to the conversation with the given id.
     *
     * @param conversationID id of the conversation
     * @return set of user ids who can write to the conversation
     */
    public Set<Integer> getPermissionValues(int conversationID) {
        try {
            String sql = "SELECT userID FROM ParticipantOf WHERE conversationID = ? AND canWrite = 1";
            PreparedStatement statement = getConnection().prepareStatement(sql);
            statement.setInt(1, conversationID);
            ResultSet rs = statement.executeQuery();

            Set<Integer> results = new HashSet<>();
            while (rs.next()) {
                results.add(rs.getInt("userID"));
            }
            return results;

        } catch (SQLException e) {
            System.out.println("Failed to getPermissionValues()");
            e.printStackTrace();
        }
        return Collections.emptySet();
    }

    /**
     * Returns true if the given user can send messages to the given conversation.
     *
     * @param conversationId
     * @param userId
     * @return true if user can write to the conversation
     */
    public boolean canWrite(int conversationId, int userId) {
        try {
            String sql = "SELECT canWrite FROM ParticipantOf WHERE userID = ? AND conversationID = ?";
            PreparedStatement statement = getConnection().prepareStatement(sql);
            statement.setInt(1, userId);
            statement.setInt(2, conversationId);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                return rs.getInt("canWrite") == 1;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }
}
