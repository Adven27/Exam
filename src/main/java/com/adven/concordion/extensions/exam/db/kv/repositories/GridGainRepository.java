package com.adven.concordion.extensions.exam.db.kv.repositories;

import com.adven.concordion.extensions.exam.db.kv.KeyValueRepository;
import com.adven.concordion.extensions.exam.entities.Entity;
import com.google.common.base.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.IgniteConfiguration;

@Slf4j
@RequiredArgsConstructor
public final class GridGainRepository implements KeyValueRepository {

    private static final int DEFAULT_RETRY_COUNT = 3;

    private final IgniteConfiguration igniteConfiguration;
    private final int retryCount;

    private Ignite igniteInstance;

    public GridGainRepository(final IgniteConfiguration igniteConfiguration) {
        this(igniteConfiguration, DEFAULT_RETRY_COUNT);
    }

    @Override
    public Optional<Object> findOne(final String cacheName, final String key) {
        final Ignite ignite = ignite();
        final IgniteCache<String, Object> cache = ignite.getOrCreateCache(cacheName);
        Object result = cache.get(key);
        log.info("Trying to get value from cache={} by key={}", cacheName, key);
        int attempts = 0;
        while (attempts <= retryCount && result == null) {
            result = cache.get(key);
            awaitFor(500);
            attempts++;
        }
        return Optional.fromNullable(result);
    }

    private void awaitFor(final long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            log.error("Thread was interrupted", e);
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public boolean save(final String cacheName, final String key, final Entity value) {
        final Ignite ignite = ignite();
        final IgniteCache<String, Object> cache = ignite.getOrCreateCache(cacheName);
        val converted = value.original();
        final boolean result;
        if (converted.isPresent()) {
            cache.put(key, converted.get());
            result = true;
        } else {
            result = false;
        }
        return result;
    }

    private Ignite ignite() {
        if (igniteInstance != null) {
            return igniteInstance;
        } else {
            igniteConfiguration.setClientMode(true);
            igniteInstance = Ignition.start(igniteConfiguration);
        }
        return igniteInstance;
    }
}
