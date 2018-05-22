package com.adven.concordion.extensions.exam.kafka.check;

/**
 * @author Ruslan Ustits
 */
public final class DummyMock implements CheckMessageMock {

    private final boolean returnValue;

    public DummyMock(final boolean returnValue) {
        this.returnValue = returnValue;
    }

    @Override
    public boolean verify() {
        return returnValue;
    }

}
