package specs.db.kv.set;

import specs.db.kv.KeyValue;

import static com.adven.concordion.extensions.exam.kafka.protobuf.TestEntity.Entity;

public class KVSetWithProtobuf extends KeyValue {

    public boolean check() {
        final Object object = inMemoryKeyValueDb.get("test.cache").get("shortKey");
        final Entity inDb = Entity.newBuilder()
                .setNumber(48)
                .setName("living in db")
                .build();
        return inDb.equals(object);
    }

}
