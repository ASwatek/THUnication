package domain;

import domain.message.Message;
import domain.message.TextMessage;
import domain.user.Student;
import domain.user.Teacher;
import domain.user.User;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ConversationTest {

    Conversation conv;
    Conversation conv2;

    @Before
    public void init(){
        User[] user = {new Student("owner", 1), new Teacher("user2", 3)};
        User owner = new Student("owner",1);
        conv = new Conversation(
                owner,false, 1, "First Conversation", 1574952210
        );
        conv.setParticipants(user);

        User[] user2 = {new Student("owner", 1), new Teacher("user2", 3), new Student("user1", 2) };
        conv2 = new Conversation(
                owner,true, 2, "Second Conversation", 1574952210
        );
        conv2.setParticipants(user2);
    }

    @Test
    public void isGroupChatTest(){
        assertEquals(false,conv.isGroupChat());
        assertEquals(true,conv2.isGroupChat());
    }

    @org.junit.Test
    public void addMessage() {
        assertEquals(0, conv.getCountMessages());
        assertEquals(0, conv2.getCountMessages());
        conv.addNewMessage(new TextMessage(1,new Student("owner", 1),"Message 1",0));
        conv2.addNewMessage(new TextMessage(1,new Student("owner", 1),"Message 1",0));
        assertEquals(1, conv.getCountMessages());
        assertEquals(1, conv2.getCountMessages());
        assertEquals("Message 1", conv.getMessageList().get(conv.getMessageList().size()-1).getContent());
        assertEquals("Message 1", conv2.getMessageList().get(conv2.getMessageList().size()-1).getContent());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void throwExceptionTest(){
        conv.getMessageList().get(-1);
        conv2.getMessageList().get(-1);
        conv2.getMessageList().get(-1);
        conv.getMessageList().get(-1);
    }

    @org.junit.Test
    public void getMessageList() {
        conv.addNewMessage(new TextMessage(1,new Student("owner", 1),"Message 1",0));
        conv2.addNewMessage(new TextMessage(1,new Student("owner", 1),"Message 1",0));
        assertEquals("Message 1", conv.getMessageList().get(0).getContent());
        assertEquals("Message 1", conv2.getMessageList().get(0).getContent());

    }

    @org.junit.Test
    public void getLastMessage() {
        conv.addNewMessage(new TextMessage(1,new Student("owner", 1),"Message 1",0));
        conv2.addNewMessage(new TextMessage(1,new Student("owner", 1),"Message 1",0));
        assertEquals("Message 1", conv.getMessageList().get(0).getContent());
        assertEquals("Message 1", conv2.getMessageList().get(0).getContent());
        conv.addNewMessage(new TextMessage(2,new Student("owner", 1),"Message 2",1));
        conv2.addNewMessage(new TextMessage(2,new Student("owner", 1),"Message 2",1));
        assertEquals("Message 2", conv.getMessageList().get(0).getContent());
        assertEquals("Message 2", conv2.getMessageList().get(0).getContent());
    }

    @org.junit.Test
    public void changeOwner() {
        assertEquals("owner", conv.getOwner().getUsername());
        assertEquals("owner", conv2.getOwner().getUsername());
        conv.changeOwner(new Teacher("user2",3));
        conv2.changeOwner(new Teacher("user2",3));
        assertEquals("user2", conv.getOwner().getUsername());
        assertEquals("user2", conv2.getOwner().getUsername());
    }

    @org.junit.Test
    public void addParticipant() {
        assertEquals(false, conv2.getParticipants().contains(new Teacher("user3", 5)));
        assertEquals(3,conv2.getParticipants().size());
        conv2.addParticipant(new Teacher("user3", 5));
        assertEquals(true, conv2.getParticipants().contains(new Teacher("user3", 5)));
        assertEquals(4,conv2.getParticipants().size());
    }

    @org.junit.Test
    public void removeParticipant() {
        assertEquals(true, conv2.getParticipants().contains(new Teacher("user2", 3)));
        assertEquals(3,conv2.getParticipants().size());
        conv2.removeParticipant(new Teacher("user2", 3));
        assertEquals(false, conv2.getParticipants().contains(new Teacher("user3", 3)));
        assertEquals(2,conv2.getParticipants().size());
    }

    @Test
    public void getCreationTimeTest() {
        assertEquals(1574952210, conv.getLastChangeTime());
    }

    @Test
    public void messageOrderTest() {
        User author = new Student("owner", 1);
        conv.addNewMessages(new Message[] {
                new TextMessage(5, author,"Message 5",1575733205),
                new TextMessage(4, author,"Message 4",1575733197),
                new TextMessage(3, author,"Message 3",1575733170)
        });

        conv.addNewMessage(new TextMessage(6, author, "Message 6", 1575733360));
        conv.addOldMessages(new Message[] {
                new TextMessage(2, author, "Message 2", 1575733077),
                new TextMessage(1, author, "Message 1", 1575733070)
        });

        int i = 6;
        for (Message message : conv.getMessages()) {
            assertEquals("Message " + i--, message.getContent());
        }
    }

    @Test
    public void messageLoadedTest() {
        assertEquals(false, conv.messagesLoaded());
        conv.setMessagesLoaded(true);
        assertEquals(true, conv.messagesLoaded());
    }
}