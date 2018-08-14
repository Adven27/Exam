package com.adven.concordion.extensions.exam.kafka.check;

import com.adven.concordion.extensions.exam.entities.Entity;
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

@Slf4j
@RequiredArgsConstructor
public final class SyncMock implements CheckMessageMock {

    private final Event<? extends Entity> eventToCheck;
    private final EventConsumer eventConsumer;
    private final Verifier verifier;

    public SyncMock(final Event<? extends Entity> eventToCheck, final EventConsumer eventConsumer) {
        this(eventToCheck, eventConsumer,
            new CompositeVerifier(
                new NullAwareVerifier(),
                new MessageVerifier()));
    }

    @Override
    public boolean verify() {
        final Event<Bytes> consumedEvent = consume();
        if (consumedEvent == null) {
            return false;
        } else {
            return verify(consumedEvent);
        }
    }

    protected boolean verify(final Event<Bytes> event) {
        return getVerifier().verify(event, eventToCheck);
    }

    private Verifier getVerifier() {
        Verifier result = verifier;
        try {
            if (eventToCheck != null && eventToCheck.getVerifier() != null && !eventToCheck.getVerifier().isEmpty()) {
                result = (Verifier) Class.forName(eventToCheck.getVerifier()).newInstance();
            }
        } catch (Exception e) {
            log.warn("Unable to instantiate custom verifier of type [" + eventToCheck.getVerifier() + "]");
        }
        return result;
    }

    protected Event<Bytes> consume() {
        final String topic = eventToCheck.getTopicName();
        log.info("Trying to consume records from={}", topic);
        return consume(topic);
    }

    protected Event<Bytes> consume(final String fromTopic) {
        if (StringUtils.isBlank(fromTopic)) {
            log.warn("Unable to consume records from topic={}", fromTopic);
            return null;
        }
        final List<Event<Bytes>> events = eventConsumer.consume(fromTopic);
        if (!events.isEmpty()) {
            final Event<Bytes> event = events.get(0);
            log.info("Got event={} from topic={}", event, fromTopic);
            return event;
        } else {
            log.info("Got no events from topic={}", fromTopic);
            return null;
        }
    }

}
