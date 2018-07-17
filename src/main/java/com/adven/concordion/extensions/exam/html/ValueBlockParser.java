package com.adven.concordion.extensions.exam.html;

import com.adven.concordion.extensions.exam.entities.Entity;
import com.adven.concordion.extensions.exam.entities.StringEntity;
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
