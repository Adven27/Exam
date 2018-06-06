package com.adven.concordion.extensions.exam.kafka;

import com.adven.concordion.extensions.exam.html.Html;
import com.adven.concordion.extensions.exam.kafka.protobuf.ProtoBlockParser;
import com.adven.concordion.extensions.exam.kafka.protobuf.ProtoEntity;
import com.google.common.base.Optional;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public final class EventBlockParser implements HtmlBlockParser<Event<ProtoEntity>> {

    static final String TOPIC_NAME = "topicName";
    static final String EVENT_KEY = "key";
    private static final String DEFAULT_EVENT_VALUE_BLOCK = "value";

    private final String valueBlockName;

    public EventBlockParser() {
        this(DEFAULT_EVENT_VALUE_BLOCK);
    }

    @Override
    public Optional<Event<ProtoEntity>> parse(final Html html) {
        final String topicName = html.attr(TOPIC_NAME);
        final String key = html.attr(EVENT_KEY);
        val headers = new HeaderBlockParser().parse(html);
        val value = html.first(valueBlockName);
        if (value == null) {
            return Optional.absent();
        }
        val proto = new ProtoBlockParser().parse(value);
        if (proto.isPresent()) {
            val message = proto.get();
            return Optional.of(Event.<ProtoEntity>builder()
                .topicName(topicName)
                .key(key)
                .message(message)
                .header(headers.isPresent() ? headers.get() : null)
                .build());
        }
        return Optional.absent();
    }

}
