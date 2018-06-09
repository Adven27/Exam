package com.adven.concordion.extensions.exam.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;

@Slf4j
@RequiredArgsConstructor
public final class StringEntity extends AbstractEntity {

    private final String value;

    @Override
    public byte[] toBytes() {
        try {
            return value.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error("Bad encoding, can't get bytes of string", e);
        }
        return new byte[]{};
    }

    @Override
    public boolean isEqualTo(final byte[] bytes) {
        try {
            final String actual = new String(bytes, "UTF-8");
            return cleanup(value).equals(cleanup(actual));
        } catch (UnsupportedEncodingException e) {
            log.error("Bad encoding, can't encode bytes to string", e);
        }
        return false;
    }

    @Override
    public String printable() {
        return value;
    }
}
