package com.adven.concordion.extensions.exam.entities;

public interface Entity {

    byte[] toBytes();

    boolean isEqualTo(final byte[] bytes);

    String printable();

}
