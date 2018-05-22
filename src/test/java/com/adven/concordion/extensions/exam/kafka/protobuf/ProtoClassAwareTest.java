package com.adven.concordion.extensions.exam.kafka.protobuf;

import com.google.common.base.Optional;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

/**
 * @author Ruslan Ustits
 */
public class ProtoClassAwareTest {

    @Test
    public void testProtoInstance() {
        final ProtoClassAware<?, ?> converter = new MockConverter(TestEntity.Entity.class);
        final Optional<?> entity = converter.protoInstance();
        assertThat(entity).isNotEqualTo(Optional.absent());
    }

    @Test(expected = NullPointerException.class)
    public void testCreateConverterInstanceWithNullClass() {
        final ProtoClassAware<?, ?> converter = new MockConverter(null);
    }

}