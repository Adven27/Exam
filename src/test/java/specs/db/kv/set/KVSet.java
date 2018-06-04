package specs.db.kv.set;

import specs.db.kv.KeyValue;

public class KVSet extends KeyValue {

    public boolean check(final String expected) {
        final Object object = inMemoryKeyValueDb.get("test.cache").get("shortKey");
        return expected.equals(object);
    }

}
