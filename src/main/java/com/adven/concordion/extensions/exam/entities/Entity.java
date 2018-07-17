package com.adven.concordion.extensions.exam.entities;

import com.google.common.base.Optional;

public interface Entity<T> {

    byte[] toBytes();

    Optional<T> original();

    boolean isEqualTo(final byte[] bytes);

    boolean isEqualTo(final Object object);

    boolean isEqualTo(final String string);

    String printable();

}
