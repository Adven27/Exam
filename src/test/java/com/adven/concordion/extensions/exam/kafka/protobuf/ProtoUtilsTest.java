package com.adven.concordion.extensions.exam.kafka.protobuf;

import com.google.common.base.Optional;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Message;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.adven.concordion.extensions.exam.RandomUtils.anyString;
import static org.assertj.core.api.Assertions.assertThat;


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

    @Test
    public void testDescriptorInstance() {
        final Optional<Descriptors.Descriptor> descriptor = ProtoUtils.descriptorInstance(TestEntity.Entity.class);
        assertThat(descriptor).isEqualTo(Optional.of(TestEntity.Entity.getDescriptor()));
    }

    @Test
    public void testDescriptorInstanceByName() {
        final Optional<Descriptors.Descriptor> descriptor = ProtoUtils.descriptorInstance(TestEntity.Entity.class.getName());
        assertThat(descriptor).isEqualTo(Optional.of(TestEntity.Entity.getDescriptor()));
    }

    @Test
    public void testDescriptorInstances() {
        final List<String> classes = Arrays.asList(TestEntity.Entity.class.getName(), Object.class.getName());
        final List<Descriptors.Descriptor> descriptors = ProtoUtils.descriptorInstances(classes);
        assertThat(descriptors).hasSize(1);
    }
}