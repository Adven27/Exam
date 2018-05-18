package com.adven.concordion.extensions.exam.kafka;

import com.adven.concordion.extensions.exam.kafka.protobuf.JsonToProto;
import com.google.common.base.Optional;
import com.google.protobuf.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.common.serialization.BytesDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.List;
import java.util.Properties;

import static org.apache.kafka.clients.consumer.ConsumerConfig.*;

/**
 * @author Ruslan Ustits
 */
@Slf4j
@RequiredArgsConstructor
public final class DefaultEventProcessor implements EventProcessor {

    private static final long DEFAULT_CONSUME_TIMEOUT = 1000L;

    private final EventConsumer eventConsumer;
    private final Properties consumerProperties;

    private Message replyEvent;

    public DefaultEventProcessor(final String kafkaBrokers) {
        this(kafkaBrokers, new DefaultEventConsumer(DEFAULT_CONSUME_TIMEOUT));
    }

    public DefaultEventProcessor(final String kafkaBrokers, final EventConsumer eventConsumer) {
        this.eventConsumer = eventConsumer;
        consumerProperties = new Properties();
        consumerProperties.put(BOOTSTRAP_SERVERS_CONFIG, kafkaBrokers);
        consumerProperties.put(GROUP_ID_CONFIG, "exam-test-consumer");
        consumerProperties.put(AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProperties.put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProperties.put(VALUE_DESERIALIZER_CLASS_CONFIG, BytesDeserializer.class.getName());
    }

    public DefaultEventProcessor withConsumerProperty(final Object key, final Object value) {
        consumerProperties.put(key, value);
        return this;
    }

    @Override
    public boolean configureReply(final Event event, final String eventClass) {
        if (StringUtils.isBlank(eventClass) || event == null) {
            log.warn("Able to convert only when event and eventClass are specified. Got event={} and class={}",
                    event, eventClass);
            return false;
        }
        final Optional<? extends Message> message = convertToProto(event, eventClass);
        if (message.isPresent()) {
            replyEvent = message.get();
            return true;
        } else {
            return false;
        }
    }

    protected Optional<Message> convertToProto(final Event event, final String eventClass) {
        try {
            final Class<Message> clazz = (Class<Message>) Class.forName(eventClass);
            final JsonToProto<Message> proto = new JsonToProto<>(clazz);
            return proto.convert(event.getMessage());
        } catch (ClassNotFoundException e) {
            log.error("Unable to find class for string={}", eventClass, e);
        }
        return Optional.absent();
    }

    @Override
    public Event consume(final String fromTopic) {
        if (StringUtils.isBlank(fromTopic)) {
            log.warn("Unable to consume records from topic={}", fromTopic);
            return null;
        }
        final List<Event> events = eventConsumer.consume(fromTopic, consumerProperties);
        if (!events.isEmpty()) {
            return events.get(0);
        } else {
            return null;
        }
    }

    @Override
    public boolean reply() {
        return false;
    }

    @Override
    public boolean send(final Event event) {
        return false;
    }
}
