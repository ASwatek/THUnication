package client.ui;

import domain.message.Message;
import domain.message.TextMessage;
import domain.user.Student;
import domain.user.Teacher;
import domain.user.User;
import org.junit.Before;
import org.junit.Test;

import java.text.Format;
import java.text.SimpleDateFormat;

import static org.junit.Assert.assertEquals;

public class HtmlClassHelperTest {

    private Message message;
    private Message message2;
    private User user;
    private User user2;

    String end = "<span id=\"end\"></span></body>\n" +"<script type=\"text/javascript\">\n" +
            "  var elmnt = document.getElementById(\"end\");\n" +
            "elmnt.scrollIntoView();" +
            "</script>"+"</body>"+
            "</html>";
    String body = "<body bgcolor='#FAFAFA'>";

    @Before
    public void init(){
        user = new Student("Student",2);
        user2 = new Teacher("Teacher", 3);
        message = new TextMessage(1, user,"<p>hi</p>",1234567890);
        message2 = new TextMessage(2,user2,"<p>hi</p>",1234567890);
    }

    @Test
    public void addNewMessage(){
        client.ui.HtmlClassHelper htmlClassHelper = new client.ui.HtmlClassHelper(user);
        assertEquals(htmlClassHelper.getBegin()+body+end,htmlClassHelper.getHtmlFullString());
        htmlClassHelper.addNewMessage(message);
        body += "<div class=\"message student\"><div><span class=\"username\">Student</span><span class=\"time\">";
        body += htmlClassHelper.formatTime(1234567890);
        body += "</span></div><div class=\"content\"><p>hi</p></div></div>";
        assertEquals(htmlClassHelper.getBegin()+body+end,htmlClassHelper.getHtmlFullString());


        htmlClassHelper.addNewMessage(message2);
        body += "<div class=\"message teacher\"><div><span class=\"username\">Teacher</span><span class=\"time\">";
        body += htmlClassHelper.formatTime(1234567890);
        body += "</span></div><div class=\"content\"><p>hi</p></div></div>";
        assertEquals(htmlClassHelper.getBegin()+body+end,htmlClassHelper.getHtmlFullString());
    }

    @Test
    public void addMessages(){
        client.ui.HtmlClassHelper htmlClassHelper = new client.ui.HtmlClassHelper(user);
        assertEquals(htmlClassHelper.getBegin()+body+end,htmlClassHelper.getHtmlFullString());
        body += "<div class=\"message teacher\"><div><span class=\"username\">Teacher</span><span class=\"time\">";
        body += htmlClassHelper.formatTime(1234567890);
        body += "</span></div><div class=\"content\"><p>hi</p></div></div>";
        body += "<div class=\"message student\"><div><span class=\"username\">Student</span><span class=\"time\">";
        body += htmlClassHelper.formatTime(1234567890);
        body += "</span></div><div class=\"content\"><p>hi</p></div></div>";
        htmlClassHelper.addMessages(new Message[]{message,message2});
        assertEquals(htmlClassHelper.getBegin()+body+end,htmlClassHelper.getHtmlFullString());
    }
}
