package com.adven.concordion.extensions.exam.kafka.protobuf;

import com.google.common.base.Optional;

public final class MockConverter extends ProtoClassAware<Object, TestEntity.Entity> {

    public MockConverter(final Class<TestEntity.Entity> protoClass) {
        super(protoClass);
    }

    @Override
    public Optional<TestEntity.Entity> convert(final Object from) {
        return Optional.of(TestEntity.Entity.getDefaultInstance());
    }
}
