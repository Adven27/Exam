package com.adven.concordion.extensions.exam.entities;

public final class MockEntity extends AbstractEntity {

    @Override
    public byte[] toBytes() {
        return new byte[0];
    }

    @Override
    public boolean isEqualTo(byte[] bytes) {
        return false;
    }

    @Override
    public String printable() {
        return null;
    }
}
