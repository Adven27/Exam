package com.adven.concordion.extensions.exam.kafka.protobuf;

import com.adven.concordion.extensions.exam.kafka.EventUtils;
import com.google.common.base.Optional;
import org.apache.kafka.common.utils.Bytes;
import org.junit.Test;

import static com.adven.concordion.extensions.exam.RandomUtils.anyInt;
import static com.adven.concordion.extensions.exam.RandomUtils.anyString;
import static com.adven.concordion.extensions.exam.kafka.protobuf.TestEntity.Entity;
import static org.assertj.core.api.Assertions.assertThat;

public class ProtoBytesToJsonTest {

    @Test
    public void testConvert() {
        final String name = anyString();
        final int number = anyInt();
        final Entity entity = Entity.newBuilder()
            .setName(name)
            .setNumber(number)
            .build();
        final ProtoBytesToJson<Entity> converter = new ProtoBytesToJson<>(Entity.class);
        final Bytes bytes = Bytes.wrap(entity.toByteArray());
        final String expected = EventUtils.goodMessage(name, number);
        assertThat(converter.convert(bytes)).isEqualTo(Optional.of(expected));
    }
}