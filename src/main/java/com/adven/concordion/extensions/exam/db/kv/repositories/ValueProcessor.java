package com.adven.concordion.extensions.exam.db.kv.repositories;

import com.google.common.base.Optional;

/**
 * @author Ruslan Ustits
 */
public interface ValueProcessor {

    Optional<String> convert(final Object value);

    boolean verify(final String first, final String second);

}
