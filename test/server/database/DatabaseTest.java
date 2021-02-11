package server.database;

import domain.Conversation;
import domain.message.Message;
import domain.message.TextMessage;
import domain.user.Student;
import domain.user.Teacher;
import domain.user.User;
import org.junit.Before;
import org.junit.Test;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

public class DatabaseTest {

    Database database = new Database() {
        @Override
        protected void connect() throws SQLException {
            connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        }
    };

    @Before
    public void setup() throws SQLException {
        PreparedStatement statement = database.getConnection().prepareStatement("CREATE TABLE User\n" +
                "(\n" +
                "\tuserID INTEGER PRIMARY KEY,\n" +
                "\tUsername TEXT,\n" +
                "\tFirstName TEXT,\n" +
                "\tLastName TEXT,\n" +
                "\tRole TEXT,\n" +
                "\tPassword Text\n" +
                ");");
        statement.executeUpdate();

        statement = database.getConnection().prepareStatement("CREATE TABLE conversation\n" +
                "(\n" +
                "    conversationID integer PRIMARY KEY,\n" +
                "    timestamp INTEGER,\n" +
                "    title TEXT,\n" +
                "    createrID INTEGER,\n" +
                "    isGroup INTEGER,\n" +
                "    FOREIGN KEY(createrID) REFERENCES user(userID) ON DELETE SET NULL\n" +
                ");");
        statement.executeUpdate();

        statement = database.getConnection().prepareStatement("CREATE TABLE Message\n" +
                "(\n" +
                "\tmessageID INTEGER PRIMARY KEY,\n" +
                "\tconversationID INTEGER,\n" +
                "\ttimestamp INTEGER,\n" +
                "\tauthorID INTEGER,\n" +
                "\tcontent TEXT,\n" +
                "    FOREIGN KEY (conversationID) REFERENCES conversation(conversationID) ON DELETE CASCADE\n" +
                ");");
        statement.executeUpdate();

        statement = database.getConnection().prepareStatement("CREATE TABLE ParticipantOf\n" +
                "(\n" +
                "\tuserID INTEGER,\n" +
                "\tJoinDate INTEGER,\n" +
                "\tconversationID integer,\n" +
                "\tcanWrite integer default 1,\n" +
                "\tPRIMARY KEY (userID, conversationID),\n" +
                "\tFOREIGN KEY(userID) REFERENCES user(userID) ON DELETE CASCADE,\n" +
                "\tFOREIGN KEY(conversationID) REFERENCES conversation(conversationID) ON DELETE CASCADE\n" +
                ");");
        statement.executeUpdate();
    }

    @Test
    public void isAvailableUsername() {
        assertEquals(true, database.isAvailableUsername("Test"));

        database.createUser("Test", "password", "Student");

        assertEquals(false, database.isAvailableUsername("Test"));
    }

    @Test
    public void login() {
        database.createUser("Test", "password", "Student");

        assertEquals(null, database.login("Test", "test"));
        assertEquals(true, database.login("Test", "password") != null);
    }

    @Test
    public void createConversation() {
        User a = new Student("Student", 1);
        User b = new Teacher("Student", 2);
        database.createUser("Student", "password", "Student");
        database.createUser("Teacher", "password", "Teacher");
        Conversation conversation = new Conversation(a, false, 1, "direct chat", 123456789);
        conversation.addParticipant(b);

        database.createConversation(conversation);

        assertEquals(1, database.getConversations(a.getId()).size());
        assertEquals(1, database.getConversations(b.getId()).size());
        assertEquals(true, database.getConversations(b.getId()).get(0).equals(conversation));

        database.addMessage(1, new TextMessage(0, a, "Test message", 123456789));
        database.addMessage(1, new TextMessage(0, b, "Test message 2", 123456999));

        List<Message> messageList = database.getMessages(1, 30);
        assertEquals(2, messageList.size());
        assertEquals("Test message 2", messageList.get(0).getContent());

        List<User> participants = database.getParticipants(1);
        assertEquals("Student", participants.get(0).getUsername());
        assertEquals("Teacher", participants.get(1).getUsername());

        database.updateLastChange(1, 999999999);
        Conversation conversation1 = database.getConversations(1).get(0);
        assertEquals(999999999, conversation1.getLastChangeTime());

        assertEquals(true, conversation1.getCanWritePermissions().contains(1));
        assertEquals(true, conversation1.getCanWritePermissions().contains(2));
        assertEquals(true, database.canWrite(1, 2));

        database.updatePermissions(1, Collections.emptySet());
        conversation1 = database.getConversations(1).get(0);

        assertEquals(false, conversation1.getCanWritePermissions().contains(1));
        assertEquals(false, conversation1.getCanWritePermissions().contains(2));
    }

    @Test
    public void getRegisteredUsers() {
        database.createUser("Student", "password", "Student");
        database.createUser("Teacher", "password", "Teacher");

        List<User> users = database.getRegisteredUsers();
        assertEquals("Student", users.get(0).getUsername());
        assertEquals("Teacher", users.get(1).getUsername());
    }
}