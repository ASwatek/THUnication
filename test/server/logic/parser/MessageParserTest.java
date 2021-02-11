package server.logic.parser;

import org.junit.Test;
import server.logic.parser.MessageParser;

import static org.junit.Assert.*;

public class MessageParserTest {

    @Test
    public void parseMarkdownSimpleText() {
        assertEquals("<p>Test</p>\n", MessageParser.parseMarkdown("Test"));
    }

    @Test
    public void parseMarkdownSimpleTextMultiline() {
        assertEquals("<p>Test<br>Test2</p>\n", MessageParser.parseMarkdown("Test \nTest2"));
    }

    @Test
    public void parseMarkdownNewParagraph() {
        assertEquals("<p>Test</p>\n<p>Test2</p>\n", MessageParser.parseMarkdown("Test \n\nTest2"));
    }

    @Test
    public void parseMarkdownTextStrongTest() {
        assertEquals("<p><strong>Test</strong></p>\n", MessageParser.parseMarkdown("**Test**"));
    }

    @Test
    public void parseMarkdownTextItalicTest() {
        assertEquals("<p><em>Test</em></p>\n", MessageParser.parseMarkdown("*Test*"));
    }

    @Test
    public void parseMarkdownTextAutoUrlTest() {
        assertEquals("<p><a href=\"https://thu.de\">https://thu.de</a></p>\n", MessageParser.parseMarkdown("https://thu.de"));
    }

    @Test
    public void parseMarkdownTextAutoEmailTest() {
        assertEquals("<p><a href=\"mailto:example@example.com\">example@example.com</a></p>\n", MessageParser.parseMarkdown("example@example.com"));
    }

    @Test
    public void parseEmojis() {
        final String DONTMODIFY = ":thumbsup:";
        String output = MessageParser.parseEmojis(DONTMODIFY);
        assertEquals("![emoji](https://lovely.uber.space/thunication/emojione/1f44d.png)",output);
        assertEquals(":thumbsup:",DONTMODIFY);
        assertEquals("![emoji](https://lovely.uber.space/thunication/emojione/1f44d.png)",MessageParser.parseEmojis(":thumbsup:"));
        assertEquals("![emoji](https://lovely.uber.space/thunication/emojione/1f44e.png)",MessageParser.parseEmojis(":thumbsdown:"));
        assertEquals("![emoji](https://lovely.uber.space/thunication/emojione/1f60d.png)",MessageParser.parseEmojis(":hearteyes:"));
        assertEquals("![emoji](https://lovely.uber.space/thunication/emojione/1f61b.png)",MessageParser.parseEmojis(":P"));
        assertEquals("![emoji](https://lovely.uber.space/thunication/emojione/1f61b.png)",MessageParser.parseEmojis(":p"));
        assertEquals(".P",MessageParser.parseEmojis(".P"));
        assertEquals(".p",MessageParser.parseEmojis(".p"));
        assertEquals("![emoji](https://lovely.uber.space/thunication/emojione/1f61c.png)",MessageParser.parseEmojis(";P"));
        assertEquals("![emoji](https://lovely.uber.space/thunication/emojione/1f61c.png)",MessageParser.parseEmojis(";p"));
        assertEquals(",P",MessageParser.parseEmojis(",P"));
        assertEquals(",p",MessageParser.parseEmojis(",p"));
        assertEquals("![emoji](https://lovely.uber.space/thunication/emojione/1f62b.png)",MessageParser.parseEmojis("Dx"));
        assertEquals("![emoji](https://lovely.uber.space/thunication/emojione/1f62d.png)",MessageParser.parseEmojis(";-;"));
        assertEquals("![emoji](https://lovely.uber.space/thunication/emojione/1f62e.png)",MessageParser.parseEmojis(":O"));
        assertEquals("![emoji](https://lovely.uber.space/thunication/emojione/1f62e.png)",MessageParser.parseEmojis(":o"));
        assertEquals(".O",MessageParser.parseEmojis(".O"));
        assertEquals(".o",MessageParser.parseEmojis(".o"));
        assertEquals("![emoji](https://lovely.uber.space/thunication/emojione/1f600.png)",MessageParser.parseEmojis(":D"));
        assertEquals("![emoji](https://lovely.uber.space/thunication/emojione/1f601.png)",MessageParser.parseEmojis(":teeth:"));
        assertEquals("![emoji](https://lovely.uber.space/thunication/emojione/1f604.png)",MessageParser.parseEmojis("x)"));
        assertEquals("![emoji](https://lovely.uber.space/thunication/emojione/1f604.png)",MessageParser.parseEmojis("X)"));
        assertEquals("x9",MessageParser.parseEmojis("x9"));
        assertEquals("X9",MessageParser.parseEmojis("X9"));
        assertEquals("![emoji](https://lovely.uber.space/thunication/emojione/1f606.png)",MessageParser.parseEmojis("xD"));
        assertEquals("![emoji](https://lovely.uber.space/thunication/emojione/1f606.png)",MessageParser.parseEmojis("XD"));
        assertEquals("![emoji](https://lovely.uber.space/thunication/emojione/1f606.png)",MessageParser.parseEmojis("xd"));
        assertEquals("![emoji](https://lovely.uber.space/thunication/emojione/1f606.png)",MessageParser.parseEmojis("Xd"));
        assertEquals("![emoji](https://lovely.uber.space/thunication/emojione/1f607.png)",MessageParser.parseEmojis(":angel:"));
        assertEquals("![emoji](https://lovely.uber.space/thunication/emojione/1f608.png)",MessageParser.parseEmojis(":devil:"));
        assertEquals("![emoji](https://lovely.uber.space/thunication/emojione/1f609.png)",MessageParser.parseEmojis(";)"));
        assertEquals("![emoji](https://lovely.uber.space/thunication/emojione/1f610.png)",MessageParser.parseEmojis(":|"));
        assertEquals("![emoji](https://lovely.uber.space/thunication/emojione/1f617.png)",MessageParser.parseEmojis(":3"));
        assertEquals("![emoji](https://lovely.uber.space/thunication/emojione/1f618.png)",MessageParser.parseEmojis(":kiss:"));
        assertEquals("![emoji](https://lovely.uber.space/thunication/emojione/1f628.png)",MessageParser.parseEmojis("O.O"));
        assertEquals("![emoji](https://lovely.uber.space/thunication/emojione/1f628.png)",MessageParser.parseEmojis("O.o"));
        assertEquals("![emoji](https://lovely.uber.space/thunication/emojione/1f628.png)",MessageParser.parseEmojis("o.O"));
        assertEquals("![emoji](https://lovely.uber.space/thunication/emojione/1f628.png)",MessageParser.parseEmojis("o.o"));
        assertEquals("![emoji](https://lovely.uber.space/thunication/emojione/1f628.png)",MessageParser.parseEmojis("O:O"));
        assertEquals("![emoji](https://lovely.uber.space/thunication/emojione/1f628.png)",MessageParser.parseEmojis("O:o"));
        assertEquals("![emoji](https://lovely.uber.space/thunication/emojione/1f628.png)",MessageParser.parseEmojis("o:O"));
        assertEquals("![emoji](https://lovely.uber.space/thunication/emojione/1f628.png)",MessageParser.parseEmojis("o:o"));
        assertEquals("![emoji](https://lovely.uber.space/thunication/emojione/1f634.png)",MessageParser.parseEmojis(":tired:"));
        assertEquals("![emoji](https://lovely.uber.space/thunication/emojione/1f635.png)",MessageParser.parseEmojis("X.X"));
        assertEquals("![emoji](https://lovely.uber.space/thunication/emojione/1f635.png)",MessageParser.parseEmojis("x.X"));
        assertEquals("![emoji](https://lovely.uber.space/thunication/emojione/1f635.png)",MessageParser.parseEmojis("X.x"));
        assertEquals("![emoji](https://lovely.uber.space/thunication/emojione/1f635.png)",MessageParser.parseEmojis("x.x"));
        assertEquals("![emoji](https://lovely.uber.space/thunication/emojione/1f635.png)",MessageParser.parseEmojis("X:X"));
        assertEquals("![emoji](https://lovely.uber.space/thunication/emojione/1f635.png)",MessageParser.parseEmojis("x:X"));
        assertEquals("![emoji](https://lovely.uber.space/thunication/emojione/1f635.png)",MessageParser.parseEmojis("X:x"));
        assertEquals("![emoji](https://lovely.uber.space/thunication/emojione/1f635.png)",MessageParser.parseEmojis("x:x"));
        assertEquals("![emoji](https://lovely.uber.space/thunication/emojione/1f643.png)",MessageParser.parseEmojis("(:"));
        assertEquals("![emoji](https://lovely.uber.space/thunication/emojione/263a.png)",MessageParser.parseEmojis(":)"));
        assertEquals("![emoji](https://lovely.uber.space/thunication/emojione/2639.png)",MessageParser.parseEmojis(":("));
        assertEquals("![emoji](https://lovely.uber.space/thunication/emojione/1f644.png)",MessageParser.parseEmojis(":eyeroll:"));
        assertEquals("![emoji](https://lovely.uber.space/thunication/emojione/1f914.png)",MessageParser.parseEmojis(":thinking:"));
        assertEquals("![emoji](https://lovely.uber.space/thunication/emojione/1f922.png)",MessageParser.parseEmojis(":sick:"));
        assertEquals("![emoji](https://lovely.uber.space/thunication/emojione/2764.png)",MessageParser.parseEmojis(":heart:"));
        assertEquals("![emoji](https://lovely.uber.space/thunication/emojione/2764.png)",MessageParser.parseEmojis(":love:"));

        assertEquals("xDa",MessageParser.parseEmojis("xDa"));
        assertEquals("![emoji](https://lovely.uber.space/thunication/emojione/1f606.png)",MessageParser.parseEmojis(" xD "));
        assertEquals("a ![emoji](https://lovely.uber.space/thunication/emojione/1f606.png) a",MessageParser.parseEmojis("a xD a"));
        assertEquals("https://mytoys.scene7.com/is/image/myToys/ext/11995137-03.jpg?$rtf_mt_prod-main_xl$",MessageParser.parseEmojis("https://mytoys.scene7.com/is/image/myToys/ext/11995137-03.jpg?$rtf_mt_prod-main_xl$"));

    }
}