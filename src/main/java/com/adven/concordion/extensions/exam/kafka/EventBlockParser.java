package com.adven.concordion.extensions.exam.kafka;

import com.adven.concordion.extensions.exam.html.Html;
import com.adven.concordion.extensions.exam.kafka.protobuf.ProtoBlockParser;
import com.google.common.base.Optional;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public final class EventBlockParser implements HtmlBlockParser<Event<Entity>> {

    static final String TOPIC_NAME = "topicName";
    static final String EVENT_KEY = "key";
    private static final String DEFAULT_EVENT_VALUE_BLOCK = "value";

    private final String valueBlockName;

    public EventBlockParser() {
        this(DEFAULT_EVENT_VALUE_BLOCK);
    }

    @Override
    public Optional<Event<Entity>> parse(final Html html) {
        val topicName = html.attr(TOPIC_NAME);
        val key = html.attr(EVENT_KEY);
        val headers = new HeaderBlockParser().parse(html);
        val valueBlock = html.first(valueBlockName);
        val builder = Event.<Entity>builder();
        builder.key(key)
            .topicName(topicName)
            .header(headers.isPresent() ? headers.get() : null);
        final Entity value;
        if (valueBlock == null) {
            value = new EmptyEntity();
        } else {
            val proto = new ProtoBlockParser().parse(valueBlock);
            if (proto.isPresent()) {
                value = proto.get();
            } else {
                value = new StringEntity(valueBlock.text());
            }
        }
        builder.message(value);
        return Optional.of(builder.build());
    }

}
