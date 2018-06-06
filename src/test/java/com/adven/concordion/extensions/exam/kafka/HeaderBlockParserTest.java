package com.adven.concordion.extensions.exam.kafka;

import com.adven.concordion.extensions.exam.html.Html;
import com.google.common.base.Optional;
import org.junit.Before;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

import static com.adven.concordion.extensions.exam.RandomUtils.anyString;
import static com.adven.concordion.extensions.exam.html.HtmlBuilder.div;
import static com.adven.concordion.extensions.exam.html.HtmlBuilder.tag;
import static org.assertj.core.api.Assertions.assertThat;

public class HeaderBlockParserTest {

    private HeaderBlockParser parser;

    @Before
    public void setUp() {
        parser = new HeaderBlockParser();
    }

    @Test
    public void testParse() throws UnsupportedEncodingException {
        final String replyToTopic = anyString();
        final String correlationId = anyString();
        final Html html = div().childs(
            tag("headers").childs(
                tag(HeaderBlockParser.REPLY_TO_TOPIC).text(replyToTopic),
                tag(HeaderBlockParser.COR_ID).text(correlationId)
            ));
        final Optional<EventHeader> result = parser.parse(html);
        assertThat(result).isEqualTo(
            Optional.of(
                new EventHeader(
                    replyToTopic.getBytes("UTF-8"),
                    correlationId.getBytes("UTF-8"))));
    }

    @Test
    public void testParseWithNoHeadersTag() {
        final Optional<EventHeader> result = parser.parse(div());
        assertThat(result).isEqualTo(Optional.absent());
    }

    @Test
    public void testParseWithNoHeadersSpecified() {
        final Optional<EventHeader> result = parser.parse(div().childs(tag("headers")));
        assertThat(result).isEqualTo(Optional.of(new EventHeader(new byte[]{}, new byte[]{})));
    }

    @Test
    public void testRetrieveTextInBytes() throws UnsupportedEncodingException {
        final String text = anyString();
        final Html html = div().text(text);
        final byte[] result = parser.retrieveTextInBytes(html);
        assertThat(result).isEqualTo(text.getBytes("UTF-8"));
    }

    @Test
    public void testRetrieveTextInBytesWithNullHtml() {
        final byte[] result = parser.retrieveTextInBytes(null);
        assertThat(result).isEmpty();
    }

}