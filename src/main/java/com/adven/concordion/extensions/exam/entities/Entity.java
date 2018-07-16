package com.adven.concordion.extensions.exam.entities;

public interface Entity {

    byte[] toBytes();

    boolean isEqualTo(final byte[] bytes);

    boolean isEqualTo(final Object object);

    boolean isEqualTo(final String string);

    String printable();

}
