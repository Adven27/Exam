package com.adven.concordion.extensions.exam.db.kv.repositories;

import com.adven.concordion.extensions.exam.db.kv.KeyValueRepository;
import com.google.common.base.Optional;
import lombok.RequiredArgsConstructor;

import java.util.Map;

/**
 * @author Ruslan Ustits
 */
@RequiredArgsConstructor
public final class InMemoryRepository implements KeyValueRepository {

    private final Map<String, Map<String, Object>> db;

    @Override
    public Optional<String> findOne(final String cacheName, final String key, final ValueProcessor converter) {
        final Map<String, Object> cache = db.get(cacheName);
        if (cache == null) {
            return Optional.absent();
        } else {
            return converter.convert(cache.get(key));
        }
    }

}
