package io.github.adven27.concordion.extensions.exam.core.logger

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.sift.Discriminator

class StickyMdcDiscriminator : Discriminator<ILoggingEvent> {
    private var isStarted = false
    private var current: String = "build/testrun"

    override fun getDiscriminatingValue(event: ILoggingEvent): String {
        event.mdcPropertyMap?.get(key)?.let { current = it }
        return current
    }

    override fun start() {
        isStarted = true
    }

    override fun stop() {
        isStarted = false
    }

    override fun isStarted(): Boolean = isStarted
    override fun getKey(): String = "testname"
}
