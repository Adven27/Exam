package com.adven.concordion.extensions.exam.kafka.check;

import com.adven.concordion.extensions.exam.entities.Entity;
import com.adven.concordion.extensions.exam.kafka.Event;
import com.adven.concordion.extensions.exam.kafka.EventHeader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.common.utils.Bytes;

import java.nio.charset.StandardCharsets;

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
        final Event<? extends Entity> eventWithNewHeader = buildReply(event);
        return reply(eventWithNewHeader);
    }

    protected Event<? extends Entity> buildReply(final Event<Bytes> event) {
        final Event<? extends Entity> eventWithNewHeader;
        if (syncMock.verify(event)) {
            eventWithNewHeader = withReply.getReplyEvent()
                .toBuilder()
                .key(event.getKey())
                .header(event.getHeader())
                .build();
        } else {
            eventWithNewHeader = withReply.getFailEvent()
                .toBuilder()
                .key(event.getKey())
                .header(event.getHeader())
                .build();
        }
        return eventWithNewHeader;
    }

    protected boolean reply(final Event<? extends Entity> event) {
        final EventHeader header = event.getHeader();
        final String replyTopic = new String(header.getReplyToTopic(), StandardCharsets.UTF_8);
        if (StringUtils.isAnyBlank(replyTopic)) {
            log.warn("Can reply only with replyTopic and correlation id. Got header={}", header);
            return false;
        } else {
            return withReply.send(replyTopic, event);
        }
    }

}
