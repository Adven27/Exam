package io.github.adven27.concordion.extensions.exam.mq

interface MqTester {
    fun start()
    fun stop()
    fun send(message: Message, params: Map<String, String>)
    fun receive(): List<Message>
    fun purge()
    fun accumulateOnRetries(): Boolean = true

    open class NOOP : MqTester {
        override fun start() = Unit
        override fun stop() = Unit
        override fun send(message: Message, params: Map<String, String>) = Unit
        override fun receive(): List<Message> = listOf()
        override fun purge() = Unit
    }

    open class Message @JvmOverloads constructor(val body: String = "", val headers: Map<String, String> = emptyMap()) {
        override fun toString() = "headers: $headers; body:$body"
    }
}
