package com.adven.concordion.extensions.exam.db.kv;

import com.adven.concordion.extensions.exam.db.kv.repositories.ValueProcessor;
import com.google.common.base.Optional;

public interface KeyValueRepository {

    Optional<Object> findOne(final String cacheName, final String key);

    boolean save(final String cacheName, final String key, final String value, final String className,
                 final ValueProcessor valueProcessor);

}
