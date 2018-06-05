package com.adven.concordion.extensions.exam.kafka;

import com.adven.concordion.extensions.exam.html.Html;
import com.adven.concordion.extensions.exam.kafka.protobuf.ProtoEntity;
import com.google.common.base.Optional;
import org.junit.Before;
import org.junit.Test;

import static com.adven.concordion.extensions.exam.RandomUtils.anyString;
import static com.adven.concordion.extensions.exam.html.HtmlBuilder.div;
import static org.assertj.core.api.Assertions.assertThat;

public class EventBlockParserTest {

    private EventBlockParser parser;

    @Before
    public void setUp() {
        parser = new EventBlockParser();
    }

    @Test
    public void testParseWithNullValue() {
        final Html html = div().attr(EventBlockParser.TOPIC_NAME, anyString())
            .attr(EventBlockParser.EVENT_KEY, anyString());
        final Optional<Event<ProtoEntity>> result = parser.parse(html);
        assertThat(result).isEqualTo(Optional.absent());
    }

}