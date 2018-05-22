package com.adven.concordion.extensions.exam.kafka.check;

import com.adven.concordion.extensions.exam.kafka.Event;
import com.adven.concordion.extensions.exam.kafka.EventConsumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * @author Ruslan Ustits
 */
@Slf4j
@RequiredArgsConstructor
public final class SyncMock implements CheckMessageMock {

    private final Event<String> messageToCheck;
    private final EventConsumer eventConsumer;

    @Override
    public boolean verify() {
        final Event<String> consumedEvent = consume(messageToCheck.getTopicName());
        if (consumedEvent == null) {
            return false;
        } else {
            final String message = consumedEvent.getMessage();
            return message != null && message.equals(messageToCheck.getMessage());
        }
    }

    protected Event<String> consume(final String fromTopic) {
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

}
