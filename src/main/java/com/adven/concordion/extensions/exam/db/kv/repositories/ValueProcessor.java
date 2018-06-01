package com.adven.concordion.extensions.exam.db.kv.repositories;

import com.google.common.base.Optional;


public interface ValueProcessor<T> {

    Optional<String> convert(final Object value);

    Optional<T> convert(final String value, final String className);

    boolean verify(final String first, final String second);

}
