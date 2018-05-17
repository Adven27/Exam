package com.adven.concordion.extensions.exam.kafka;

/**
 * @author Ruslan Ustits
 */
public interface EventVerifier {

    boolean verify(final Event expected, final Event actual);

}
