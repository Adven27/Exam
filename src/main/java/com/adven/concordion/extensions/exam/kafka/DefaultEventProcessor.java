package com.adven.concordion.extensions.exam.kafka;

import com.adven.concordion.extensions.exam.kafka.protobuf.JsonToProto;
import com.google.common.base.Optional;
import com.google.protobuf.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayDeque;
import java.util.List;

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

    private ArrayDeque<Event<Message>> replyEvents = new ArrayDeque<>();

    public DefaultEventProcessor(final String kafkaBrokers) {
        this(new DefaultEventConsumer(DEFAULT_CONSUME_TIMEOUT, kafkaBrokers),
                new DefaultEventProducer(DEFAULT_PRODUCER_TIMEOUT, kafkaBrokers));
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
    public Event<String> consume(final String fromTopic) {
        if (StringUtils.isBlank(fromTopic)) {
            log.warn("Unable to consume records from topic={}", fromTopic);
            return null;
        }
        final List<Event<String>> events = eventConsumer.consume(fromTopic);
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
    public boolean send(final Event<String> event, final String eventClass) {
        final boolean isConfigured = configureReply(event, eventClass);
        return isConfigured && reply();
    }

    protected boolean send(final String topic, final String key, final Message message) {
        final boolean result;
        if (StringUtils.isBlank(topic) || message == null) {
            log.warn("Unable to send record with topic={}, key={}, message={}. Missing required parameters",
                    topic, key, message);
            result = false;
        } else {
            result = eventProducer.produce(topic, key, message);
        }
        return result;
    }

}
