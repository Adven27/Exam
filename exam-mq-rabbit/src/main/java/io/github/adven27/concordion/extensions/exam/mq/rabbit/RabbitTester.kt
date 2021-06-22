package io.github.adven27.concordion.extensions.exam.mq.rabbit

import com.rabbitmq.client.AlreadyClosedException
import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.GetResponse
import com.rabbitmq.client.MessageProperties.MINIMAL_BASIC
import io.github.adven27.concordion.extensions.exam.mq.MqTester
import mu.KLogging
import java.io.IOException
import java.nio.charset.StandardCharsets.UTF_8
import java.util.Optional
import java.util.function.Function

@Suppress("unused")
open class RabbitTester @JvmOverloads constructor(
    protected val connectionFactory: ConnectionFactory = ConnectionFactory().apply {
        host = DEFAULT_HOST
        port = DEFAULT_PORT
    },
    protected val sendConfig: SendConfig,
    protected val receiveConfig: ReceiveConfig,
) : MqTester {
    private lateinit var connection: Connection
    private lateinit var ch: Channel

    @JvmOverloads
    constructor(
        host: String,
        port: Int = 5672,
        sendConfig: SendConfig,
        receiveConfig: ReceiveConfig,
    ) : this(
        connectionFactory = ConnectionFactory().apply {
            this.host = host
            this.port = port
        },
        sendConfig = sendConfig,
        receiveConfig = receiveConfig,
    )

    constructor(
        port: Int,
        sendConfig: SendConfig,
        receiveConfig: ReceiveConfig,
    ) : this(
        connectionFactory = ConnectionFactory().apply {
            this.host = DEFAULT_HOST
            this.port = port
        },
        sendConfig = sendConfig,
        receiveConfig = receiveConfig,
    )

    override fun purge() {
        receiveConfig.queues.forEach {
            val ok = ch.queuePurge(it)
            logger.info("Purged {} messages", ok.messageCount)
        }
    }

    override fun receive(): List<MqTester.Message> {
        logger.info("Receiving message from {}...", receiveConfig.queues[0])
        val result: MutableList<MqTester.Message> = mutableListOf()
        var response: Optional<GetResponse>
        do {
            response = Optional.ofNullable(ch.basicGet(receiveConfig.queues[0], true))
            response.ifPresent { result.add(receiveConfig.receiveConverter.apply(it)!!) }
        } while (response.isPresent)
        logger.info("Got messages: {}", result)
        return result
    }

    override fun send(message: MqTester.Message, params: Map<String, String>) {
        logger.info("Publishing message to {} routing key {} : {}", sendConfig.exchange, sendConfig.routingKey, message)
        ch.basicPublish(
            sendConfig.exchange,
            sendConfig.routingKey,
            MINIMAL_BASIC,
            sendConfig.sendConverter.apply(message)
        )
    }

    override fun start() {
        connection = connectionFactory.newConnection()
        receiveConfig.queues.forEach { declareQueueIfMissing(connection, it) }
        ch = this.connection.createChannel()
        purge()
    }

    override fun stop() {
        try {
            connection.close(CONNECTION_CLOSE_TIMEOUT)
        } catch (ignored: AlreadyClosedException) {
        }
    }

    private fun declareQueueIfMissing(connection: Connection?, queue: String?) {
        try {
            val ch = connection!!.createChannel()
            ch.queueDeclarePassive(queue)
            ch.close()
        } catch (ex: IOException) {
            fun queueDoesNotExist(e: IOException) = e.cause != null && e.cause!!.message!!.contains("NOT_FOUND")
            if (queueDoesNotExist(ex)) {
                ch = connection!!.createChannel()
                ch.queueDeclare(queue, true, false, false, null)
                ch.close()
            } else {
                throw ex
            }
        }
    }

    interface SendConverter : Function<MqTester.Message?, ByteArray?>
    interface ReceiveConverter : Function<GetResponse?, MqTester.Message?>

    data class SendConfig @JvmOverloads constructor(
        val exchange: String = "",
        val routingKey: String,
        val sendConverter: SendConverter = DEFAULT_SEND_CONVERTER,
    ) {
        constructor(routingKey: String, sendConverter: SendConverter = DEFAULT_SEND_CONVERTER) :
            this("", routingKey, sendConverter)
    }

    data class ReceiveConfig @JvmOverloads constructor(
        val queues: List<String>,
        val receiveConverter: ReceiveConverter = DEFAULT_RECEIVE_CONVERTER
    ) {
        @JvmOverloads
        constructor(queue: String, receiveConverter: ReceiveConverter = DEFAULT_RECEIVE_CONVERTER) :
            this(listOf(queue), receiveConverter)
    }

    companion object : KLogging() {
        const val DEFAULT_HOST = "localhost"
        const val DEFAULT_PORT = 5672
        const val CONNECTION_CLOSE_TIMEOUT = 4000
        val DEFAULT_SEND_CONVERTER: SendConverter = object : SendConverter {
            override fun apply(body: MqTester.Message?): ByteArray? = body?.body?.toByteArray(UTF_8)
        }
        val DEFAULT_RECEIVE_CONVERTER: ReceiveConverter = object : ReceiveConverter {
            override fun apply(response: GetResponse?): MqTester.Message? = MqTester.Message(String(response?.body!!))
        }
    }
}
