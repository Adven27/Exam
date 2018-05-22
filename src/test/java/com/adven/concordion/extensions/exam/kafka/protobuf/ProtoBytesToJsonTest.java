package com.adven.concordion.extensions.exam.kafka.protobuf;

import com.google.common.base.Optional;
import org.apache.kafka.common.utils.Bytes;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import static com.adven.concordion.extensions.exam.kafka.protobuf.TestEntity.Entity;
import static org.assertj.core.api.Assertions.*;

/**
 * @author Ruslan Ustits
 */
public class ProtoBytesToJsonTest {

    @Test
    public void testConvert() {
        final Entity entity = Entity.newBuilder()
                .setName("name")
                .setNumber(6)
                .build();
        final ProtoBytesToJson<Entity> converter = new ProtoBytesToJson<>(Entity.class);
        final Bytes bytes = Bytes.wrap(entity.toByteArray());
        final String expected = "{\n" +
                "  \"name\": \"name\",\n" +
                "  \"number\": 6\n" +
                "}";
        assertThat(converter.convert(bytes)).isEqualTo(Optional.of(expected));
    }
}