package com.adven.concordion.extensions.exam.utils.protobuf;

import com.google.common.base.Optional;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ProtoClassAwareTest {

    @Test
    public void testProtoInstance() {
        final ProtoClassAware<?, ?> converter = new MockConverter(TestEntity.Entity.class);
        final Optional<?> entity = converter.protoInstance();
        assertThat(entity).isNotEqualTo(Optional.absent());
    }

    @Test(expected = NullPointerException.class)
    public void testCreateConverterInstanceWithNullClass() {
        new MockConverter(null);
    }

}