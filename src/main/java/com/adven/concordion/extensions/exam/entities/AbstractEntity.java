package com.adven.concordion.extensions.exam.entities;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter(AccessLevel.PROTECTED)
@RequiredArgsConstructor
public abstract class AbstractEntity<T> implements Entity<T> {

    private final String value;

    @Override
    public boolean isEqualTo(@NonNull final String string) {
        final String expected = cleanup(value);
        final String actual = cleanup(string);
        return expected.equals(actual);
    }

    @Override
    public String printable() {
        return value;
    }

    protected final String cleanup(@NonNull final String message) {
        return message.replaceAll("\\s", "");
    }

}
