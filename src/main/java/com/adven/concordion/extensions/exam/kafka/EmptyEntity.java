package com.adven.concordion.extensions.exam.kafka;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public final class EmptyEntity implements Entity {

    @Override
    public byte[] toBytes() {
        return new byte[0];
    }

    @Override
    public boolean isEqualTo(final byte[] bytes) {
        return bytes == null || bytes.length == 0;
    }

    @Override
    public String printable() {
        return "<empty>";
    }

}
