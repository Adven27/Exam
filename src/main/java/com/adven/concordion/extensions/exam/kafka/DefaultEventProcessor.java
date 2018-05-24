package com.adven.concordion.extensions.exam.kafka;

import com.adven.concordion.extensions.exam.kafka.check.*;
import com.adven.concordion.extensions.exam.kafka.protobuf.ProtoUtils;
import com.google.common.base.Optional;
import com.google.protobuf.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

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

    public DefaultEventProcessor(final String kafkaBrokers) {
        this(new DefaultEventConsumer(DEFAULT_CONSUME_TIMEOUT, kafkaBrokers),
                new DefaultEventProducer(DEFAULT_PRODUCER_TIMEOUT, kafkaBrokers));
    }

    @Override
    public boolean check(final Event<String> eventToCheck, final String eventToCheckClass, final boolean isAsync) {
        return checkWithReply(eventToCheck, eventToCheckClass, null,
                null, null, isAsync);
    }

    @Override
    public boolean checkWithReply(final Event<String> eventToCheck, final String eventToCheckClass,
                                  final Event<String> replySuccessEvent, final Event<String> replyFailEvent,
                                  final String replyEventClass, final boolean isAsync) {
        final SyncMock syncMock = new SyncMock(eventToCheck, eventToCheckClass, eventConsumer);
        CheckMessageMock mock = syncMock;
        if (replySuccessEvent != null) {
            final Optional<WithReply> withReplyMock = mockWithReply(replySuccessEvent, replyFailEvent,
                    replyEventClass, syncMock);
            if (withReplyMock.isPresent()) {
                mock = new ReplyWithTopicFromHeader(syncMock, withReplyMock.get());
            } else {
                return false;
            }
        }
        if (isAsync) {
            mock = new AsyncMock(mock);
        }
        return mock.verify();
    }

    protected Optional<WithReply> mockWithReply(final Event<String> replySuccessEvent, final Event<String> replyFailEvent,
                                                final String replyEventClass, final CheckMessageMock mock) {
        final Optional<Event<Message>> successEvent = ProtoUtils.fromJsonToProto(replySuccessEvent, replyEventClass);
        final Optional<Event<Message>> failEvent = ProtoUtils.fromJsonToProto(replyFailEvent, replyEventClass);
        if (successEvent.isPresent() && failEvent.isPresent()) {
            return Optional.of(new WithReply(successEvent.get(), failEvent.get(), eventProducer, mock));
        } else {
            log.warn("Unable to convert reply messages");
            return Optional.absent();
        }
    }

    @Override
    public boolean send(final Event<String> event, final String eventClass) {
        if (StringUtils.isBlank(eventClass) || event == null) {
            log.warn("Able to convert only when event and eventClass are specified. Got event={} and class={}",
                    event, eventClass);
            return false;
        }
        final boolean result;
        final Optional<Message> protoMessage = ProtoUtils.fromJsonToProto(event.getMessage(), eventClass);
        if (protoMessage.isPresent()) {
            result = send(event.getTopicName(), event.getKey(), protoMessage.get());
        } else {
            result = false;
        }
        return result;
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
