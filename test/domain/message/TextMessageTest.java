package domain.message;

import domain.user.Student;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TextMessageTest {

    Message textMessage;

    @Before
    public void init(){
        textMessage = new TextMessage(1,new Student("author",1), "Message 1",0);
    }

    @Test
    public void getContentTest() {
        assertEquals("Message 1", textMessage.getContent());
    }

    @Test
    public void getIdTest(){
        assertEquals(1, textMessage.getId());
    }

    @Test
    public void getAuthorTest(){
        assertEquals(new Student("author",1),textMessage.getAuthor());
    }

    @Test
    public void getTimestampTest() {
        assertEquals(0, textMessage.getTimestamp());
    }

    @Test
    public void setContentTest() {
        assertEquals("Message 1", textMessage.getContent());
        textMessage.setContent("Message 1 Modified");
        assertEquals("Message 1 Modified", textMessage.getContent());
    }
}