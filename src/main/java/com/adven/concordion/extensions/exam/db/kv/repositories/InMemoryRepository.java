package com.adven.concordion.extensions.exam.db.kv.repositories;

import com.adven.concordion.extensions.exam.db.kv.KeyValueRepository;
import com.adven.concordion.extensions.exam.entities.Entity;
import com.google.common.base.Optional;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public final class InMemoryRepository implements KeyValueRepository {

    private final Map<String, Map<String, Object>> db;

    @Override
    public Optional<Object> findOne(final String cacheName, final String key) {
        val cache = db.get(cacheName);
        return Optional.fromNullable(cache.get(key));
    }

    @Override
    public boolean save(final String cacheName, final String key, final Entity value) {
        if (!db.containsKey(cacheName)) {
            db.put(cacheName, new HashMap<String, Object>());
        }
        val converted = value.original();
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
