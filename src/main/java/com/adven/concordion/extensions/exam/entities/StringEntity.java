package com.adven.concordion.extensions.exam.entities;

import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;

@Slf4j
public final class StringEntity extends AbstractEntity {

    public StringEntity(final String value) {
        super(value);
    }

    @Override
    public byte[] toBytes() {
        try {
            return getValue().getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error("Bad encoding, can't get bytes of string", e);
        }
        return new byte[]{};
    }

    @Override
    public boolean isEqualTo(final byte[] bytes) {
        try {
            final String actual = new String(bytes, "UTF-8");
            return isEqualTo(actual);
        } catch (UnsupportedEncodingException e) {
            log.error("Bad encoding, can't encode bytes to string", e);
        }
        return false;
    }

    @Override
    public boolean isEqualTo(final Object object) {
        String cast = null;
        try {
            cast = String.class.cast(object);
        } catch (ClassCastException e) {
            log.error("Failed to cast value={} to string", getValue());
        }
        return cast != null && isEqualTo(cast);
    }

}
