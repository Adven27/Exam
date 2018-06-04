package com.adven.concordion.extensions.exam.kafka.protobuf;

import com.google.common.base.Optional;
import com.google.protobuf.Message;
import org.junit.Test;

import static com.adven.concordion.extensions.exam.RandomUtils.anyString;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Ruslan Ustits
 */
public class ProtoUtilsTest {

    @Test
    public void testSafeForName() {
        final Optional<Class<Message>> clazz = ProtoUtils.safeForName(TestEntity.Entity.class.getName());
        assertThat(clazz).isEqualTo(Optional.of(TestEntity.Entity.class));
    }

    @Test
    public void testSafeForNameWithBadClassName() {
        final Optional<Class<Message>> clazz = ProtoUtils.safeForName(anyString());
        assertThat(clazz).isEqualTo(Optional.absent());
    }

}