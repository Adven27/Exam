package com.adven.concordion.extensions.exam.entities;

import com.google.common.base.Optional;

public final class MockEntity extends AbstractEntity {

    public MockEntity(final String value) {
        super(value);
    }

    @Override
    public byte[] toBytes() {
        return new byte[0];
    }

    @Override
    public Optional original() {
        return Optional.absent();
    }

    @Override
    public boolean isEqualTo(byte[] bytes) {
        return false;
    }

    @Override
    public boolean isEqualTo(Object object) {
        return false;
    }

    @Override
    public String printable() {
        return null;
    }
}
