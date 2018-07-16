package com.adven.concordion.extensions.exam.utils.protobuf;

import com.google.common.base.Optional;
import org.junit.Before;
import org.junit.Test;

import static com.adven.concordion.extensions.exam.utils.protobuf.TestEntity.Entity;
import static org.assertj.core.api.Assertions.assertThat;

public class ProtoToJsonTest {

    private ProtoToJson<Entity> converter;

    @Before
    public void setUp() throws Exception {
        converter = new ProtoToJson<>();
    }

    @Test
    public void testConvert() {
        final Entity entity = Entity.newBuilder()
            .setName("123")
            .setNumber(321)
            .build();
        final String expected = "{\n"
            + "  \"name\": \"123\",\n"
            + "  \"number\": 321\n"
            + "}";
        final Optional<String> result = converter.convert(entity);
        assertThat(result).isEqualTo(Optional.of(expected));
    }

    @Test
    public void testConvertWithNull() {
        final Optional<String> result = converter.convert(null);
        assertThat(result).isEqualTo(Optional.absent());
    }

}