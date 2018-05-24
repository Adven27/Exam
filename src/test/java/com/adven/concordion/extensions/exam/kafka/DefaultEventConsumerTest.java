package com.adven.concordion.extensions.exam.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.utils.Bytes;
import org.junit.Before;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Properties;

import static com.adven.concordion.extensions.exam.RandomUtils.anyString;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Ruslan Ustits
 */
public class DefaultEventConsumerTest {

    private DefaultEventConsumer consumer;

    @Before
    public void setUp() {
        consumer = new DefaultEventConsumer(1000L, new Properties());
    }

    @Test
    public void toEvent() throws UnsupportedEncodingException {
        final String topic = anyString();
        final String key = anyString();
        final String message = anyString();
        final Bytes messageInBytes = new Bytes(message.getBytes(Charset.forName("UTF-8")));
        final ConsumerRecord<String, Bytes> record = new ConsumerRecord<>(topic, 1, 1L, key, messageInBytes);
        final Event<Bytes> event = consumer.toEvent(record);
        assertThat(event.getKey()).isEqualTo(key);
        assertThat(event.getTopicName()).isEqualTo(topic);
        assertThat(event.getMessage()).isEqualTo(Bytes.wrap(message.getBytes("UTF-8")));
    }

    @Test(expected = NullPointerException.class)
    public void testToEventWithNullRecord() {
        consumer.toEvent(null);
    }

    @Test(expected = NullPointerException.class)
    public void testConsumeWithNullTopic() {
        consumer.consume(null);
    }

    @Test(expected = NullPointerException.class)
    public void testCreateInstanceWithNullProperty() {
        new DefaultEventConsumer(10L, (Properties) null);
    }

    @Test(expected = NullPointerException.class)
    public void testEventHeaderWithNull() {
        consumer.eventHeader(null);
    }

    @Test
    public void testHeaderToString() throws UnsupportedEncodingException {
        final String value = anyString();
        final Header header = new RecordHeader(anyString(), value.getBytes("UTF-8"));
        final String result = consumer.headerToString(header);
        assertThat(result).isEqualTo(value);
    }

    @Test
    public void testHeaderToStringWithNullHeader() throws UnsupportedEncodingException {
        final String result = consumer.headerToString(null);
        assertThat(result).isEqualTo("");
    }

}