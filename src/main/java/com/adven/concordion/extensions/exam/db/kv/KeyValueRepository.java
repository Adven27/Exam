package com.adven.concordion.extensions.exam.db.kv;

import com.adven.concordion.extensions.exam.db.kv.repositories.ValueProcessor;
import com.google.common.base.Optional;

public interface KeyValueRepository {

    Optional<String> findOne(final String cacheName, final String key, final ValueProcessor<?> valueProcessor);

    boolean save(final String cacheName, final String key, final String value, final String className,
                 final ValueProcessor valueProcessor);

}
