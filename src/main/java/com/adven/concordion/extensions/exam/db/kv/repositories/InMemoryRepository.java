package com.adven.concordion.extensions.exam.db.kv.repositories;

import com.adven.concordion.extensions.exam.db.kv.KeyValueRepository;
import com.google.common.base.Optional;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public final class InMemoryRepository implements KeyValueRepository {

    private final Map<String, Map<String, Object>> db;

    @Override
    public Optional<String> findOne(final String cacheName, final String key, final ValueProcessor<?> converter) {
        val cache = db.get(cacheName);
        if (cache == null) {
            return Optional.absent();
        } else {
            return converter.convert(cache.get(key));
        }
    }

    @Override
    public boolean save(final String cacheName, final String key, final String value, final String className,
                        final ValueProcessor valueProcessor) {
        if (!db.containsKey(cacheName)) {
            db.put(cacheName, new HashMap<String, Object>());
        }
        val converted = valueProcessor.convert(value, className);
        final boolean result;
        if (converted.isPresent()) {
            db.get(cacheName).put(key, converted.get());
            result = true;
        } else {
            result = false;
        }
        return result;
    }
}
