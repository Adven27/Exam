package com.adven.concordion.extensions.exam.kafka.protobuf;

import com.google.common.base.Optional;
import org.apache.kafka.common.utils.Bytes;
import org.junit.Before;
import org.junit.Test;

import static com.adven.concordion.extensions.exam.RandomUtils.anyInt;
import static com.adven.concordion.extensions.exam.RandomUtils.anyString;
import static com.adven.concordion.extensions.exam.kafka.protobuf.TestEntity.Entity;
import static org.assertj.core.api.Assertions.assertThat;

public class BytesToProtoTest {

    private BytesToProto<Entity> converter;

    @Before
    public void setUp() throws Exception {
        converter = new BytesToProto<>(Entity.class);
    }

    @Test
    public void testConvertWithNull() {
        assertThat(converter.convert(null)).isEqualTo(Optional.absent());
    }

    @Test
    public void testConvertWithEmptyBytesArray() {
        final Bytes bytes = Bytes.wrap(new byte[]{});
        assertThat(converter.convert(bytes)).isEqualTo(Optional.absent());
    }

    @Test
    public void testParse() {
        final Entity expected = Entity.newBuilder()
            .setName(anyString())
            .setNumber(anyInt())
            .build();
        final byte[] bytes = expected.toByteArray();
        final Optional<Entity> entity = converter.parse(bytes);
        assertThat(entity).isEqualTo(Optional.of(expected));
    }

}