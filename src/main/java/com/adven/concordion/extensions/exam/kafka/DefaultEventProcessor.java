package com.adven.concordion.extensions.exam.kafka;

import com.adven.concordion.extensions.exam.kafka.protobuf.JsonToProto;
import com.google.common.base.Optional;
import com.google.protobuf.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.common.serialization.BytesDeserializer;
import org.apache.kafka.common.serialization.BytesSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Properties;

import static org.apache.kafka.clients.consumer.ConsumerConfig.*;
import static org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG;

/**
 * @author Ruslan Ustits
 */
@Slf4j
@RequiredArgsConstructor
public final class DefaultEventProcessor implements EventProcessor {

    private static final long DEFAULT_CONSUME_TIMEOUT = 1000L;
    private static final long DEFAULT_PRODUCER_TIMEOUT = 1000L;

    private final EventConsumer eventConsumer;
    private final EventProducer eventProducer;
    private final Properties consumerProperties;
    private final Properties producerProperties;

    private ArrayDeque<Event<Message>> replyEvents;

    public DefaultEventProcessor(final String kafkaBrokers) {
        this(kafkaBrokers, new DefaultEventConsumer(DEFAULT_CONSUME_TIMEOUT),
                new DefaultEventProducer(DEFAULT_PRODUCER_TIMEOUT));
    }

    public DefaultEventProcessor(final String kafkaBrokers, final EventConsumer eventConsumer,
                                 final EventProducer eventProducer) {
        this.eventConsumer = eventConsumer;
        this.eventProducer = eventProducer;
        consumerProperties = new Properties();
        consumerProperties.put(BOOTSTRAP_SERVERS_CONFIG, kafkaBrokers);
        consumerProperties.put(GROUP_ID_CONFIG, "exam-test-consumer-group");
        consumerProperties.put(CLIENT_ID_CONFIG, "exam-test-consumer");
        consumerProperties.put(AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProperties.put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProperties.put(VALUE_DESERIALIZER_CLASS_CONFIG, BytesDeserializer.class.getName());

        producerProperties = new Properties();
        producerProperties.put(BOOTSTRAP_SERVERS_CONFIG, kafkaBrokers);
        producerProperties.put(CLIENT_ID_CONFIG, "exam-test-producer");
        producerProperties.put(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProperties.put(VALUE_SERIALIZER_CLASS_CONFIG, BytesSerializer.class.getName());

        replyEvents = new ArrayDeque<>();
    }

    public DefaultEventProcessor withConsumerProperty(final Object key, final Object value) {
        consumerProperties.put(key, value);
        return this;
    }

    public DefaultEventProcessor withProducerProperty(final Object key, final Object value) {
        producerProperties.put(key, value);
        return this;
    }

    @Override
    public boolean configureReply(final Event<String> event, final String eventClass) {
        if (StringUtils.isBlank(eventClass) || event == null) {
            log.warn("Able to convert only when event and eventClass are specified. Got event={} and class={}",
                    event, eventClass);
            return false;
        }
        final Optional<? extends Message> message = convertToProto(event, eventClass);
        if (message.isPresent()) {
            replyEvents.push(
                    Event.<Message>builder()
                            .topicName(event.getTopicName())
                            .key(event.getKey())
                            .message(message.get())
                            .build());
            return true;
        } else {
            return false;
        }
    }

    protected Optional<Message> convertToProto(final Event<String> event, final String eventClass) {
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
        final Event<Message> reply = replyEvents.poll();
        final boolean result;
        if (reply == null) {
            log.warn("No reply event message specified, unable to reply");
            result = false;
        } else {
            result = send(reply.getTopicName(), reply.getKey(), reply.getMessage());
        }
        return result;
    }

    @Override
    public boolean send(final Event<String> event) {
        return false;
    }

    protected boolean send(final String topic, final String key, final Message message) {
        final boolean result;
        if (StringUtils.isBlank(topic) || message == null) {
            log.warn("Unable to send record with topic={}, key={}, message={}. Missing required parameters",
                    topic, key, message);
            result = false;
        } else {
            result = eventProducer.produce(topic, key, message, producerProperties);
        }
        return result;
    }

}
