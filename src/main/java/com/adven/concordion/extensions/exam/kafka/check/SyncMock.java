package com.adven.concordion.extensions.exam.kafka.check;

import com.adven.concordion.extensions.exam.kafka.Event;
import com.adven.concordion.extensions.exam.kafka.EventConsumer;
import com.adven.concordion.extensions.exam.kafka.protobuf.ProtoBytesToJson;
import com.google.common.base.Optional;
import com.google.protobuf.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.common.utils.Bytes;

import java.util.List;

/**
 * @author Ruslan Ustits
 */
@Slf4j
@RequiredArgsConstructor
public final class SyncMock implements CheckMessageMock {

    private final Event<String> messageToCheck;
    private final String protobufClass;
    private final EventConsumer eventConsumer;

    @Override
    public boolean verify() {
        final Event<Bytes> consumedEvent = consume(messageToCheck.getTopicName());
        if (consumedEvent == null) {
            return false;
        } else {
            final Optional<String> message = convertToJson(consumedEvent.getMessage());
            return message.isPresent() && message.get().equals(messageToCheck.getMessage());
        }
    }

    protected Event<Bytes> consume(final String fromTopic) {
        if (StringUtils.isBlank(fromTopic)) {
            log.warn("Unable to consume records from topic={}", fromTopic);
            return null;
        }
        final List<Event<Bytes>> events = eventConsumer.consume(fromTopic);
        if (!events.isEmpty()) {
            return events.get(0);
        } else {
            return null;
        }
    }

    private Optional<String> convertToJson(final Bytes message) {
        final ProtoBytesToJson<? extends Message> converter = ProtoBytesToJson.forProtoClass(protobufClass);
        if (converter != null) {
            return converter.convert(message);
        } else {
            return Optional.absent();
        }
    }

}
