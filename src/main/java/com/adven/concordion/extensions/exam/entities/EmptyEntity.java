package com.adven.concordion.extensions.exam.entities;

import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

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
    public boolean isEqualTo(final Object object) {
        return object == null || isEqualTo((String) object);
    }

    @Override
    public boolean isEqualTo(final String string) {
        return StringUtils.isBlank(string);
    }

    @Override
    public String printable() {
        return "<empty>";
    }

}
