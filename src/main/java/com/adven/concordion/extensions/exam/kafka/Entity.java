package com.adven.concordion.extensions.exam.kafka;

public interface Entity {

    byte[] toBytes();

    boolean isEqualTo(final byte[] bytes);

}
