package com.adven.concordion.extensions.exam.kafka;

import com.adven.concordion.extensions.exam.kafka.check.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
@RequiredArgsConstructor
public final class DefaultEventProcessor implements EventProcessor {

    private static final long DEFAULT_CONSUME_TIMEOUT = 1000L;
    private static final long DEFAULT_PRODUCER_TIMEOUT = 1000L;

    private final EventConsumer eventConsumer;
    private final EventProducer eventProducer;

    public DefaultEventProcessor(final String kafkaBrokers) {
        this(new DefaultEventConsumer(DEFAULT_CONSUME_TIMEOUT, kafkaBrokers),
            new DefaultEventProducer(DEFAULT_PRODUCER_TIMEOUT, kafkaBrokers));
    }

    @Override
    public boolean check(final Event<? extends Entity> eventToCheck, final boolean isAsync) {
        return checkWithReply(eventToCheck, null, null, isAsync);
    }

    @Override
    public boolean checkWithReply(final Event<? extends Entity> eventToCheck,
                                  final Event<? extends Entity> replySuccessEvent,
                                  final Event<? extends Entity> replyFailEvent,
                                  final boolean isAsync) {
        final SyncMock syncMock = new SyncMock(eventToCheck, eventConsumer);
        CheckMessageMock mock = syncMock;
        if (replySuccessEvent != null && replyFailEvent != null) {
            mock = new ReplyWithTopicFromHeader(syncMock,
                new WithReply(replySuccessEvent, replyFailEvent, eventProducer, syncMock));
        }
        if (isAsync) {
            mock = new AsyncMock(mock);
        }
        return mock.verify();
    }

    @Override
    public boolean send(final Event<? extends Entity> event) {
        if (event == null) {
            log.warn("Can't send null event");
            return false;
        }
        return send(event.getTopicName(), event.getKey(), event.getMessage(), event.getHeader());
    }

    protected boolean send(final String topic, final String key, final Entity message,
                           final EventHeader header) {
        if (StringUtils.isBlank(topic) || message == null) {
            log.warn("Unable to send record with topic={}, key={}, message={}. Missing required parameters",
                topic, key, message);
            return false;
        }
        return eventProducer.produce(topic, key, header, message);
    }
}
