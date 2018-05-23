package com.adven.concordion.extensions.exam.kafka.check;

import com.adven.concordion.extensions.exam.kafka.Event;
import com.adven.concordion.extensions.exam.kafka.EventConsumer;
import com.adven.concordion.extensions.exam.kafka.check.verify.CompositeVerifier;
import com.adven.concordion.extensions.exam.kafka.check.verify.MessageVerifier;
import com.adven.concordion.extensions.exam.kafka.check.verify.NullAwareVerifier;
import com.adven.concordion.extensions.exam.kafka.check.verify.Verifier;
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
    private final EventConsumer eventConsumer;
    private final Verifier verifier;

    public SyncMock(final Event<String> messageToCheck, final String protobufClass,
                    final EventConsumer eventConsumer) {
        this(messageToCheck, eventConsumer,
                new CompositeVerifier(
                        new NullAwareVerifier(),
                        new MessageVerifier(protobufClass)));
    }

    @Override
    public boolean verify() {
        final Event<Bytes> consumedEvent = consume(messageToCheck.getTopicName());
        if (consumedEvent == null) {
            return false;
        } else {
            return verifier.verify(consumedEvent, messageToCheck);
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

}
