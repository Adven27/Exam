package com.adven.concordion.extensions.exam.html.parsers;

import com.adven.concordion.extensions.exam.html.Html;
import com.adven.concordion.extensions.exam.kafka.Entity;
import com.adven.concordion.extensions.exam.kafka.HtmlBlockParser;
import com.adven.concordion.extensions.exam.kafka.StringEntity;
import com.adven.concordion.extensions.exam.kafka.protobuf.ProtoBlockParser;
import com.google.common.base.Optional;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public final class ValueBlockParser implements HtmlBlockParser<Entity> {

    private final String blockName;

    @Override
    public Optional<Entity> parse(final Html html) {
        val valueBlock = html.first(blockName);
        final Entity value;
        if (valueBlock == null) {
            return Optional.absent();
        } else {
            val proto = new ProtoBlockParser().parse(valueBlock);
            if (proto.isPresent()) {
                value = proto.get();
            } else {
                value = new StringEntity(valueBlock.text());
            }
        }
        return Optional.of(value);
    }

}
