package client.ui;

import domain.message.Message;
import domain.message.TextMessage;
import domain.user.User;
import domain.user.Student;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Scanner;

public class HtmlClassHelper {

    // TIME OFFSET
    private long offset = ZonedDateTime.now().getHour() * 3600 + ZonedDateTime.now().getMinute() * 60 + ZonedDateTime.now().getSecond() - (System.currentTimeMillis() / 1000) % 86400;

    private User currentUser;
    private Format format = new SimpleDateFormat("dd.MM.yyyy',' HH:mm");
    private String[] days = new String[]{"Thursday", "Friday", "Saturday", "Sunday", "Monday", "Tuesday", "Wednesday"};
    private String begin = "<html><head><link rel=\"stylesheet\" type=\"text/css\" href=\"" + getClass().getResource("htmlchatstyle.css").toExternalForm() + "\"/></head>";
    private String end = "<span id=\"end\"></span></body>\n" + "<script type=\"text/javascript\">\n" +
            "  var elmnt = document.getElementById(\"end\");\n" +
            "elmnt.scrollIntoView();" +
            "</script>" + "</body>" +
            "</html>";
    private String body = "<body bgcolor='#FAFAFA'>";

    public HtmlClassHelper(User currentUser) {
        this.currentUser = currentUser;
    }

    //needed for Ui test
    public String getBegin() {
        return begin;
    }

    /**
     * Convert content into a html format
     * @param message content
     */
    public void addNewMessage(Message message) {
        StringBuilder stringBuilder = new StringBuilder(body);

        String cssClasses = "";
        if (message.getAuthor() instanceof Student) cssClasses += " student";
        else cssClasses += " teacher";

        stringBuilder.append("<div class=\"message").append(cssClasses).append("\"><div><span class=\"username\">");
        stringBuilder.append(message.getAuthor().getUsername());  // In case we need the `you` state we can compare the author against the current user
        stringBuilder.append("</span><span class=\"time\">");
        stringBuilder.append(formatTime(message.getTimestamp()));
        stringBuilder.append("</span></div>");
        stringBuilder.append("<div class=\"content\">");
        stringBuilder.append(message.getContent());
        stringBuilder.append("</div></div>");
        body = stringBuilder.toString();
    }

    /**
     * Support addNewMessage for more messages
     * @param messages more than one message
     */
    public void addMessages(Message[] messages) {
        // The newest message is at index 0.
        for (int i = messages.length - 1; i >= 0; i--) {
            this.addNewMessage(messages[i]);
        }
    }

    /**
     * @return the complete html as a String
     */
    public String getHtmlFullString() {
        StringBuilder stringBuilder = new StringBuilder(begin);
        stringBuilder.append(body);
        stringBuilder.append(end);
        return stringBuilder.toString();
    }

    /**
     * Formats the given timestamp to a relative date string.
     *
     * @param timestamp
     * @return
     */
    String formatTime(long timestamp) {
        // Message is not older than 6 days, display day in a relative way.
        if (((System.currentTimeMillis() + offset) / 1000) < (timestamp / 1000) + 518400) {
            long actualTime = System.currentTimeMillis() / 1000 + offset;
            long messageTime = timestamp / 1000 + offset;
            long distance = actualTime - messageTime;
            if (distance < actualTime % 86400) {
                return new SimpleDateFormat("'Today, 'HH:mm").format(timestamp);
            } else if ((distance < (86400 + actualTime % 86400))) {
                return new SimpleDateFormat("'Yesterday, 'HH:mm").format(timestamp);
            } else {
                return new SimpleDateFormat("'" + days[(int) ((messageTime % 604800) / 86400)] + ", 'HH:mm").format(timestamp);
            }
        }
        // Message is older than 6 days display full date.
        return format.format(timestamp);
    }
}
