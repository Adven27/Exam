package com.adven.concordion.extensions.exam.kafka.check;

import com.adven.concordion.extensions.exam.kafka.Event;
import com.adven.concordion.extensions.exam.kafka.EventHeader;
import com.google.protobuf.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.common.utils.Bytes;

/**
 * @author Ruslan Ustits
 */
@Slf4j
@RequiredArgsConstructor
public final class ReplyWithTopicFromHeader implements CheckMessageMock {

    private final SyncMock syncMock;
    private final WithReply withReply;

    @Override
    public boolean verify() {
        final Event<Bytes> event = syncMock.consume();
        if (event == null) {
            return false;
        }
        final Event<Message> eventWithNewHeader = buildReply(event);
        return reply(eventWithNewHeader);
    }

    protected Event<Message> buildReply(final Event<Bytes> event) {
        final Event<Message> eventWithNewHeader;
        if (syncMock.verify(event)) {
            eventWithNewHeader = withReply.getReplyEvent()
                    .toBuilder()
                    .header(event.getHeader())
                    .build();
        } else {
            eventWithNewHeader = withReply.getFailEvent()
                    .toBuilder()
                    .header(event.getHeader())
                    .build();
        }
        return eventWithNewHeader;
    }

    protected boolean reply(final Event<Message> event) {
        final EventHeader header = event.getHeader();
        final String replyTopic = header.getReplyToTopic();
        final String corId = header.getCorrelationId();
        if (StringUtils.isAnyBlank(replyTopic, corId)) {
            log.warn("Can reply only with replyTopic and correlation id. Got header={}", header);
            return false;
        } else {
            return withReply.send(event.getHeader().getReplyToTopic(), event);
        }
    }

}
