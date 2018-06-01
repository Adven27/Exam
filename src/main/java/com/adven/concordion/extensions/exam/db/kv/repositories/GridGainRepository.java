package com.adven.concordion.extensions.exam.db.kv.repositories;

import com.adven.concordion.extensions.exam.db.kv.KeyValueRepository;
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

    private final IgniteConfiguration igniteConfiguration;

    private Ignite igniteInstance;

    @Override
    public Optional<String> findOne(final String cacheName, final String key, final ValueProcessor<?> valueProcessor) {
        final Ignite ignite = ignite();
        final IgniteCache<String, Object> cache = ignite.getOrCreateCache(cacheName);
        log.info("Trying to get value from cache={} by key={}", cacheName, key);
        final Object entity = cache.get(key);
        return valueProcessor.convert(entity);
    }

    @Override
    public boolean save(final String cacheName, final String key, final String value, final String className,
                        final ValueProcessor valueProcessor) {
        final Ignite ignite = ignite();
        final IgniteCache<String, Object> cache = ignite.getOrCreateCache(cacheName);
        val converted = valueProcessor.convert(value, className);
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
