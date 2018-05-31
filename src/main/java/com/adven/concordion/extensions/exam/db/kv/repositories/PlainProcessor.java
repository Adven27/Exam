package com.adven.concordion.extensions.exam.db.kv.repositories;

import com.google.common.base.Optional;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Ruslan Ustits
 */
@Slf4j
public final class PlainProcessor implements ValueProcessor {

    @Override
    public Optional<String> convert(final Object value) {
        String cast = null;
        try {
            cast = String.class.cast(value);
        } catch (ClassCastException e) {
            log.error("Failed to cast value={} to string", value);
        }
        return Optional.fromNullable(cast);
    }

    @Override
    public boolean verify(final String first, final String second) {
        if (first == null || second == null) {
            return false;
        }
        return first.equals(second);
    }
}
