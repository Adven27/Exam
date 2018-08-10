package com.adven.concordion.extensions.exam.kafka.check;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public final class AsyncMock implements Runnable, CheckMessageMock {

    private final CheckMessageMock messageMock;

    @Override
    public void run() {
        messageMock.verify();
    }

    @Override
    public boolean verify() {
        new Thread(this, "EventCheck-Mock-Thread").start();
        return true;
    }

}
