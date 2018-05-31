package com.adven.concordion.extensions.exam.db.kv.repositories;

import com.adven.concordion.extensions.exam.db.kv.KeyValueRepository;
import com.google.common.base.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.IgniteConfiguration;

/**
 * @author Ruslan Ustits
 */
@Slf4j
@RequiredArgsConstructor
public final class GridGainRepository implements KeyValueRepository {

    private final IgniteConfiguration igniteConfiguration;

    @Override
    public Optional<String> findOne(final String cacheName, final String key, final ValueProcessor valueProcessor) {
        igniteConfiguration.setClientMode(true);
        final Ignite ignite = Ignition.start(igniteConfiguration);
        final IgniteCache<String, Object> cache = ignite.getOrCreateCache(cacheName);
        log.info("Trying to get value from cache={} by key={}", cacheName, key);
        final Object entity = cache.get(key);
        return valueProcessor.convert(entity);
    }

}
