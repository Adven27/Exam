package com.adven.concordion.extensions.exam.kafka;

import com.adven.concordion.extensions.exam.kafka.protobuf.JsonToProto;
import com.google.common.base.Optional;
import com.google.protobuf.Message;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Ruslan Ustits
 */
@Slf4j
public final class DefaultEventProcessor implements EventProcessor {

    private Message replyEvent;

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
        return null;
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
