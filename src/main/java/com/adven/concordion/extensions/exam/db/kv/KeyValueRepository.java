package com.adven.concordion.extensions.exam.db.kv;

import com.adven.concordion.extensions.exam.entities.Entity;
import com.google.common.base.Optional;

public interface KeyValueRepository {

    Optional<Object> findOne(final String cacheName, final String key);

    boolean save(final String cacheName, final String key, final Entity entity);

}
