package com.adven.concordion.extensions.exam.db.kv;

import com.google.common.base.Optional;

/**
 * @author Ruslan Ustits
 */
public interface KeyValueRepository {

    Optional<String> findOne(final String key);

}
