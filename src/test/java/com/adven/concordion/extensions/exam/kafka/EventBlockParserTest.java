package com.adven.concordion.extensions.exam.kafka;

import com.adven.concordion.extensions.exam.entities.Entity;
import com.adven.concordion.extensions.exam.html.Html;
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
        final String topicName = anyString();
        final String key = anyString();
        final Html html = div().attr(EventBlockParser.TOPIC_NAME, topicName)
            .attr(EventBlockParser.EVENT_KEY, key);
        final Optional<Event<Entity>> result = parser.parse(html);
        assertThat(result).isNotEqualTo(Optional.absent());
        assertThat(result.get().getMessage()).isEqualTo(new EmptyEntity());
    }

}