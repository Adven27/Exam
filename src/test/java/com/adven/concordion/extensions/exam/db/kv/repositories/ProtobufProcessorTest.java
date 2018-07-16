package com.adven.concordion.extensions.exam.db.kv.repositories;

import com.google.common.base.Optional;
import net.javacrumbs.jsonunit.core.Configuration;
import org.junit.Before;
import org.junit.Test;

import static com.adven.concordion.extensions.exam.RandomUtils.anyInt;
import static com.adven.concordion.extensions.exam.RandomUtils.anyString;
import static com.adven.concordion.extensions.exam.utils.protobuf.TestEntity.Entity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;


public class ProtobufProcessorTest {

    private ProtobufProcessor processor;

    @Before
    public void setUp() {
        processor = new ProtobufProcessor(mock(Configuration.class));
    }

    @Test
    public void testConvert() {
        final Entity entity = Entity.newBuilder()
                .setName(anyString())
                .setNumber(anyInt())
                .build();
        final Optional<String> result = processor.convert(entity);
        assertThat(result).isNotEqualTo(Optional.absent());
    }

    @Test
    public void testConvertWithNotProtobufMessage() {
        final Optional<String> result = processor.convert(anyString());
        assertThat(result).isEqualTo(Optional.absent());
    }

}