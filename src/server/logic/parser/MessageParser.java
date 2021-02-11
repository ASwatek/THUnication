package server.logic.parser;

import org.commonmark.Extension;
import org.commonmark.ext.autolink.AutolinkExtension;
import org.commonmark.node.Image;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.AttributeProvider;
import org.commonmark.renderer.html.AttributeProviderContext;
import org.commonmark.renderer.html.AttributeProviderFactory;
import org.commonmark.renderer.html.HtmlRenderer;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Contains methods for parsing chat messages.
 */
public class MessageParser {
    /**
     * Shorthand function to perform all steps of parsing a message.
     *
     * @param message simple string
     * @return parsed message in html
     */
    public static String parse(String message) {
        return parseMarkdown(parseEmojis(message));
    }

    /**
     * Parses the given message with `Attlassian CommonMark parser`.
     *
     * @param message simple string
     * @return parsed message in html
     */
    public static String parseMarkdown(String message) {
        List<Extension> extensions = Collections.singletonList(AutolinkExtension.create());
        Parser parser = Parser.builder().extensions(extensions).build();
        Node document = parser.parse(message);
        HtmlRenderer renderer = HtmlRenderer.builder()
                .escapeHtml(true)
                .softbreak("<br>")
                .attributeProviderFactory(context -> (node, s, attributes) -> {
                    if (node instanceof Image) {
                        if (attributes.containsValue("emoji")) {
                            attributes.put("style", "height:23px;");
                        }
                    }
                })
                .build();
        return renderer.render(document);
    }

    /**
     * Converts all instances of popular text emojis to their unicode equivalent.
     *
     * @param message string with possible text emojis to be converted to unicode emojis
     * @return string containing unicode emojis
     */
    public static String parseEmojis(String message) {
        String conv = " " + message + " ";
        conv = conv.replaceAll(" :thumbsup: ", " ![emoji](https://lovely.uber.space/thunication/emojione/1f44d.png) ");
        conv = conv.replaceAll(" :thumbsdown: ", "![emoji](https://lovely.uber.space/thunication/emojione/1f44e.png) ");
        conv = conv.replaceAll(" :hearteyes: ", " ![emoji](https://lovely.uber.space/thunication/emojione/1f60d.png) ");
        conv = conv.replaceAll(" :smug: ", " ![emoji](https://lovely.uber.space/thunication/emojione/1f60e.png) ");
        conv = conv.replaceAll(" (?i):P ", " ![emoji](https://lovely.uber.space/thunication/emojione/1f61b.png) ");
        conv = conv.replaceAll(" (?i);P ", " ![emoji](https://lovely.uber.space/thunication/emojione/1f61c.png) ");
        conv = conv.replaceAll(" Dx ", " ![emoji](https://lovely.uber.space/thunication/emojione/1f62b.png) ");
        conv = conv.replaceAll(" ;-; ", " ![emoji](https://lovely.uber.space/thunication/emojione/1f62d.png) ");
        conv = conv.replaceAll(" (?i):O ", " ![emoji](https://lovely.uber.space/thunication/emojione/1f62e.png) ");
        conv = conv.replaceAll(" :D ", " ![emoji](https://lovely.uber.space/thunication/emojione/1f600.png) ");
        conv = conv.replaceAll(" :teeth: ", " ![emoji](https://lovely.uber.space/thunication/emojione/1f601.png) ");
        conv = conv.replaceAll(" (?i)x\\) ", " ![emoji](https://lovely.uber.space/thunication/emojione/1f604.png) ");
        conv = conv.replaceAll(" (?i)xD ", " ![emoji](https://lovely.uber.space/thunication/emojione/1f606.png) ");
        conv = conv.replaceAll(" :angel: ", " ![emoji](https://lovely.uber.space/thunication/emojione/1f607.png) ");
        conv = conv.replaceAll(" :devil: ", " ![emoji](https://lovely.uber.space/thunication/emojione/1f608.png) ");
        conv = conv.replaceAll(" ;\\) ", " ![emoji](https://lovely.uber.space/thunication/emojione/1f609.png) ");
        conv = conv.replaceAll(" :\\| ", " ![emoji](https://lovely.uber.space/thunication/emojione/1f610.png) ");
        conv = conv.replaceAll(" :3 ", " ![emoji](https://lovely.uber.space/thunication/emojione/1f617.png) ");
        conv = conv.replaceAll(" :kiss: ", " ![emoji](https://lovely.uber.space/thunication/emojione/1f618.png) ");
        conv = conv.replaceAll(" (?i)O.(?i)O ", " ![emoji](https://lovely.uber.space/thunication/emojione/1f628.png) ");
        conv = conv.replaceAll(" :tired: ", " ![emoji](https://lovely.uber.space/thunication/emojione/1f634.png) ");
        conv = conv.replaceAll(" (?i)X.(?i)X ", " ![emoji](https://lovely.uber.space/thunication/emojione/1f635.png) ");
        conv = conv.replaceAll(" \\(: ", " ![emoji](https://lovely.uber.space/thunication/emojione/1f643.png) ");
        conv = conv.replaceAll(" :\\) ", " ![emoji](https://lovely.uber.space/thunication/emojione/263a.png) ");
        conv = conv.replaceAll(" :\\( ", " ![emoji](https://lovely.uber.space/thunication/emojione/2639.png) ");
        conv = conv.replaceAll(" :eyeroll: ", " ![emoji](https://lovely.uber.space/thunication/emojione/1f644.png) ");
        conv = conv.replaceAll(" :thinking: ", " ![emoji](https://lovely.uber.space/thunication/emojione/1f914.png) ");
        conv = conv.replaceAll(" :sick: ", " ![emoji](https://lovely.uber.space/thunication/emojione/1f922.png) ");
        conv = conv.replaceAll(" :heart: ", " ![emoji](https://lovely.uber.space/thunication/emojione/2764.png) ");
        conv = conv.replaceAll(" :love: ", " ![emoji](https://lovely.uber.space/thunication/emojione/2764.png) ");
        return conv.trim();
    }

    /**
     * Forbid the creation of this class.
     */
    private MessageParser() {
    }
}
