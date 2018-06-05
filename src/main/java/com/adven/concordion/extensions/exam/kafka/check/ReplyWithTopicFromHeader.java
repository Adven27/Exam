package com.adven.concordion.extensions.exam.kafka.check;

import com.adven.concordion.extensions.exam.kafka.Event;
import com.adven.concordion.extensions.exam.kafka.EventHeader;
import com.adven.concordion.extensions.exam.kafka.protobuf.ProtoEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.common.utils.Bytes;

import java.io.UnsupportedEncodingException;

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
        final Event<ProtoEntity> eventWithNewHeader = buildReply(event);
        return reply(eventWithNewHeader);
    }

    protected Event<ProtoEntity> buildReply(final Event<Bytes> event) {
        final Event<ProtoEntity> eventWithNewHeader;
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

    protected boolean reply(final Event<ProtoEntity> event) {
        try {
            final EventHeader header = event.getHeader();
            final String replyTopic = new String(header.getReplyToTopic(), "UTF-8");
            if (StringUtils.isAnyBlank(replyTopic)) {
                log.warn("Can reply only with replyTopic and correlation id. Got header={}", header);
                return false;
            } else {
                return withReply.send(replyTopic, event);
            }
        } catch (UnsupportedEncodingException e) {
            log.error("Unable to encode replyTopic={} to UTF-8", e);

        }
        return false;
    }

}