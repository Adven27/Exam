package com.adven.concordion.extensions.exam.kafka;


public interface EventVerifier {

    boolean verify(final Event expected, final Event actual);

}
