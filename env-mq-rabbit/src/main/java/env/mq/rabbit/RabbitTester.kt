package env.mq.rabbit

import com.adven.concordion.extensions.exam.mq.MqTester
import com.rabbitmq.client.AlreadyClosedException
import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.GetResponse
import com.rabbitmq.client.MessageProperties.MINIMAL_BASIC
import mu.KLogging
import java.io.IOException
import java.nio.charset.StandardCharsets.UTF_8
import java.util.Optional
import java.util.concurrent.TimeoutException
import java.util.function.Function

open class RabbitTester @JvmOverloads constructor(
    protected val exchange: String?,
    protected val routingKey: String?,
    protected val queue: List<String?>,
    protected val port: Int,
    protected val sendConverter: SendConverter = DEFAULT_SEND_CONVERTER,
    protected val receiveConverter: ReceiveConverter = DEFAULT_RECEIVE_CONVERTER
) : MqTester {
    private lateinit var connection: Connection
    private lateinit var ch: Channel

    @JvmOverloads
    constructor(
        queue: String,
        port: Int,
        sendConverter: SendConverter = DEFAULT_SEND_CONVERTER,
        receiveConverter: ReceiveConverter = DEFAULT_RECEIVE_CONVERTER
    ) : this(null, null, listOf<String?>(queue), port, sendConverter, receiveConverter)

    constructor(exchange: String?, routingKey: String?, queue: String?, port: Int) : this(
        exchange,
        routingKey,
        listOf(queue),
        port,
        DEFAULT_SEND_CONVERTER,
        DEFAULT_RECEIVE_CONVERTER
    )

    constructor(exchange: String?, routingKey: String?, queue: String?, port: Int, sendConverter: SendConverter) : this(
        exchange,
        routingKey,
        listOf(queue),
        port,
        sendConverter,
        DEFAULT_RECEIVE_CONVERTER
    )

    constructor(
        exchange: String,
        routingKey: String,
        queue: List<String>,
        port: Int,
        receiveConverter: ReceiveConverter
    ) : this(exchange, routingKey, queue, port, DEFAULT_SEND_CONVERTER, receiveConverter)

    constructor(
        exchange: String,
        routingKey: String,
        queue: String,
        port: Int,
        receiveConverter: ReceiveConverter
    ) : this(exchange, routingKey, listOf(queue), port, DEFAULT_SEND_CONVERTER, receiveConverter)

    override fun purge() {
        queue.forEach {
            val ok = ch.queuePurge(it)
            logger.info("Purged {} messages", ok.messageCount)
        }
    }

    override fun receive(): List<MqTester.Message> {
        logger.info("Receiving message from {}...", queue[0])
        val result: MutableList<MqTester.Message> = mutableListOf()
        var response: Optional<GetResponse>
        do {
            response = Optional.ofNullable(ch.basicGet(queue[0], true))
            response.ifPresent { result.add(receiveConverter.apply(it)!!) }
        } while (response.isPresent)
        logger.info("Got messages: {}", result)
        return result
    }

    override fun send(message: String, headers: Map<String, String>) {
        logger.info("Publishing message to {} routing key {} : {}", exchange, routingKey, message)
        ch.basicPublish(
            exchange,
            routingKey,
            MINIMAL_BASIC,
            sendConverter.apply(MqTester.Message(message, headers))
        )
    }

    override fun start() {
        val factory = ConnectionFactory()
        factory.port = port
        connection = factory.newConnection()
        queue.forEach { declareQueueIfMissing(connection, it) }
        ch = this.connection.createChannel()
        purge()
    }

    override fun stop() {
        try {
            connection.close()
        } catch (ignored: AlreadyClosedException) {
        }
    }

    @Throws(TimeoutException::class, IOException::class)
    private fun declareQueueIfMissing(connection: Connection?, queue: String?) {
        try {
            val ch = connection!!.createChannel()
            ch.queueDeclarePassive(queue)
            ch.close()
        } catch (maybeQueueDoesNotExist: IOException) {
            if (maybeQueueDoesNotExist.cause != null && maybeQueueDoesNotExist.cause!!.message!!.contains("NOT_FOUND")) {
                ch = connection!!.createChannel()
                ch.queueDeclare(queue, true, false, false, null)
                ch.close()
            } else {
                throw maybeQueueDoesNotExist
            }
        }
    }

    interface SendConverter : Function<MqTester.Message?, ByteArray?>
    interface ReceiveConverter : Function<GetResponse?, MqTester.Message?>
    companion object : KLogging() {
        val DEFAULT_SEND_CONVERTER: SendConverter = object : SendConverter {
            override fun apply(body: MqTester.Message?): ByteArray? = body?.body?.toByteArray(UTF_8)
        }
        val DEFAULT_RECEIVE_CONVERTER: ReceiveConverter = object : ReceiveConverter {
            override fun apply(response: GetResponse?): MqTester.Message? = MqTester.Message(String(response?.body!!))
        }
    }
}
