package com.adven.concordion.extensions.exam.kafka;

import com.adven.concordion.extensions.exam.kafka.protobuf.ProtoEntity;
import org.apache.kafka.common.utils.Bytes;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public final class DummyEventConsumer implements EventConsumer {

    private final List<Event<Bytes>> events;

    private DummyEventConsumer(final List<Event<Bytes>> events) {
        this.events = events;
    }

    public static DummyEventConsumer returning(final Event<Bytes>... events) {
        return new DummyEventConsumer(Arrays.asList(events));
    }

    public static DummyEventConsumer defaultInstance() {
        return new DummyEventConsumer(new ArrayList<Event<Bytes>>());
    }

    public DummyEventConsumer addEventToReturn(final Event<Bytes> event) {
        events.add(event);
        return this;
    }

    public DummyEventConsumer addProtoEventToReturn(final Event<ProtoEntity> event) {
        final ProtoEntity message = event.getMessage();
        final Event<Bytes> eventToReturn = Event.<Bytes>builder()
                .message(message == null ? null : Bytes.wrap(message.toBytes()))
                .topicName(event.getTopicName())
                .key(event.getKey())
                .build();
        return addEventToReturn(eventToReturn);
    }

    @Override
    public List<Event<Bytes>> consume(final String fromTopic) {
        if (fromTopic == null) {
            throw new NullPointerException();
        }
        return events;
    }

}
