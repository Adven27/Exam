package com.adven.concordion.extensions.exam.kafka.protobuf;

import com.google.common.base.Optional;
import com.google.protobuf.Message;
import org.junit.Before;
import org.junit.Test;

import static com.adven.concordion.extensions.exam.kafka.protobuf.TestEntity.Entity;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Ruslan Ustits
 */
public class JsonToProtoTest {

    private JsonToProto<Entity> converter;

    @Before
    public void setUp() {
        converter = new JsonToProto<>(Entity.class);
    }

    @Test
    public void testConvertWithNullString() {
        final Optional<Entity> result = converter.convert(null);
        assertThat(result).isEqualTo(Optional.absent());
    }

    @Test
    public void testBuildMessageBuilder() {
        final Optional<Message.Builder> builder = converter.buildMessageBuilder();
        assertThat(builder.get()).isNotNull();
    }

    @Test
    public void testbuildProtoToObject() {
        final Entity expected = Entity.newBuilder()
                .setName("123")
                .setNumber(321)
                .build();
        final String json = "{ name: \"123\", number: 321 }";
        final Entity result = converter.buildProtoObject(json, Entity.newBuilder());
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void testBuildProtoToObjectWithBadJson() {
        final Entity result = converter.buildProtoObject("bad json", Entity.newBuilder());
        assertThat(result).isNull();
    }

}