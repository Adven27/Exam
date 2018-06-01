package specs.db.kv.check;

import com.adven.concordion.extensions.exam.kafka.protobuf.TestEntity;
import specs.db.kv.KeyValue;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Ruslan Ustits
 */
public class KVCheckWithProtobuf extends KeyValue {

    public void insertValue(final String cacheName, final String key, final String name, final int number) {
        final TestEntity.Entity entity = TestEntity.Entity.newBuilder()
                .setName(name)
                .setNumber(number)
                .build();
        final Map<String, Object> cache = new HashMap<>();
        cache.put(key, entity);
        inMemoryKeyValueDb.put(cacheName, cache);
    }

}
