package specs.db.kv.check;

import specs.db.kv.KeyValue;

import java.util.HashMap;
import java.util.Map;


public class KVCheck extends KeyValue {

    public void insertValue(final String cacheName, final String key, final String value) {
        final Map<String, Object> cache = new HashMap<>();
        cache.put(key, value);
        inMemoryKeyValueDb.put(cacheName, cache);
    }

}
