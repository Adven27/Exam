package com.adven.concordion.extensions.exam.mq.ibmmq

import com.adven.concordion.extensions.exam.mq.MqTester
import com.ibm.msg.client.jms.JmsFactoryFactory
import com.ibm.msg.client.wmq.WMQConstants.WMQ_CHANNEL
import com.ibm.msg.client.wmq.WMQConstants.WMQ_CM_CLIENT
import com.ibm.msg.client.wmq.WMQConstants.WMQ_CONNECTION_MODE
import com.ibm.msg.client.wmq.WMQConstants.WMQ_HOST_NAME
import com.ibm.msg.client.wmq.WMQConstants.WMQ_PORT
import com.ibm.msg.client.wmq.WMQConstants.WMQ_PROVIDER
import com.ibm.msg.client.wmq.WMQConstants.WMQ_QUEUE_MANAGER
import mu.KLogging
import java.util.function.Function
import javax.jms.Message
import javax.jms.MessageConsumer
import javax.jms.MessageProducer
import javax.jms.Queue
import javax.jms.QueueBrowser
import javax.jms.Session
import javax.jms.TextMessage

/**
 * Receive uses a {@sample QueueBrowser} object to look at messages on a queue without removing them.
 * @see javax.jms.QueueBrowser
 */
@Suppress("unused")
open class IbmMQBrowseAndSendTester @JvmOverloads constructor(
    config: Config,
    private val sendConverter: SendConverter = DEFAULT_SEND_CONVERTER,
    receiveConverter: ReceiveConverter = DEFAULT_RECEIVE_CONVERTER
) : IbmMQBrowseOnlyTester(config, receiveConverter) {
    companion object : KLogging()

    protected lateinit var producer: MessageProducer

    override fun send(message: String, headers: Map<String, String>) {
        logger.info("Sending to {}...", config.queue)
        producer.send(
            sendConverter.apply(MqTester.Message(message, headers) to session).apply {
                jmsCorrelationID = headers["jmsCorrelationID"]
            }
        )
        logger.info("Sent to {}\n{}", config.queue, message)
    }

    override fun afterQueueCreation(queue: Queue) {
        super.afterQueueCreation(queue)
        producer = session.createProducer(queue)
    }
}

/**
 * Receive uses a {@sample MessageConsumer} object to consume messages from a queue with removing.
 * @see javax.jms.MessageConsumer
 */
@Suppress("unused")
open class IbmMQConsumeAndSendTester @JvmOverloads constructor(
    config: Config,
    private val sendConverter: SendConverter = DEFAULT_SEND_CONVERTER,
    receiveConverter: ReceiveConverter = DEFAULT_RECEIVE_CONVERTER
) : IbmMQConsumeOnlyTester(config, receiveConverter) {
    companion object : KLogging()

    protected lateinit var producer: MessageProducer

    override fun send(message: String, headers: Map<String, String>) {
        logger.info("Sending to {}...", config.queue)
        producer.send(
            sendConverter.apply(MqTester.Message(message, headers) to session).apply {
                jmsCorrelationID = headers["jmsCorrelationID"]
            }
        )
        logger.info("Sent to {}\n{}", config.queue, message)
    }

    override fun afterQueueCreation(queue: Queue) {
        super.afterQueueCreation(queue)
        producer = session.createProducer(queue)
    }
}

/**
 * Receive uses a {@sample QueueBrowser} object to look at messages on a queue without removing them.
 * Send does nothing.
 * @see javax.jms.QueueBrowser
 */
open class IbmMQBrowseOnlyTester @JvmOverloads constructor(
    config: Config,
    private val receiveConverter: ReceiveConverter = DEFAULT_RECEIVE_CONVERTER
) : JmsTester(config) {
    companion object : KLogging()

    protected lateinit var browser: QueueBrowser

    override fun receive(): List<MqTester.Message> {
        logger.info("Reading from {}", config.queue)
        val result: MutableList<MqTester.Message> = mutableListOf()
        val messages = browser.enumeration
        while (messages.hasMoreElements()) {
            result.add(messages.nextElement().let { receiveConverter.apply(it as Message) })
        }
        return result
    }

    override fun afterQueueCreation(queue: Queue) {
        super.afterQueueCreation(queue)
        browser = session.createBrowser(queue)
    }
}

/**
 * Receive uses a {@sample MessageConsumer} object to consume messages from a queue with removing.
 * Send does nothing.
 * @see javax.jms.MessageConsumer
 */
open class IbmMQConsumeOnlyTester @JvmOverloads constructor(
    config: Config,
    private val receiveConverter: ReceiveConverter = DEFAULT_RECEIVE_CONVERTER
) : JmsTester(config) {
    companion object : KLogging()

    override fun receive(): List<MqTester.Message> {
        logger.info("Reading from {}", config.queue)
        val result: MutableList<MqTester.Message> = mutableListOf()
        while (true) {
            val message = consumer.receiveNoWait() ?: break
            result.add(message.let { receiveConverter.apply(it) })
        }
        return result
    }
}

abstract class JmsTester(protected val config: Config) : MqTester.NOOP() {
    protected lateinit var session: Session
    protected lateinit var consumer: MessageConsumer

    override fun start() {
        with(connectionFactory().createConnection()) {
            session = createSession(false, Session.AUTO_ACKNOWLEDGE)
            config.queue.let { q -> session.createQueue(queueName(q)).also { afterQueueCreation(it) } }
            start()
        }
        purge()
        logger.info("MqTester started with config $config")
    }

    private fun queueName(q: String) = if (q.startsWith("queue://")) q else "queue:///$q"

    override fun stop() {
        session.close()
    }

    override fun purge() {
        logger.info("Purging queue ${config.queue}...")
        while (consumer.receiveNoWait() != null) {
            logger.info("Message dropped...")
        }
        logger.info("Queue ${config.queue} is purged.")
    }

    private fun connectionFactory() = JmsFactoryFactory.getInstance(WMQ_PROVIDER).createConnectionFactory().apply {
        setStringProperty(WMQ_HOST_NAME, config.host)
        setIntProperty(WMQ_PORT, config.port)
        setStringProperty(WMQ_CHANNEL, config.channel)
        setStringProperty(WMQ_QUEUE_MANAGER, config.manager)
        setIntProperty(WMQ_CONNECTION_MODE, WMQ_CM_CLIENT)
    }

    open fun afterQueueCreation(queue: Queue) {
        consumer = session.createConsumer(queue)
    }

    data class Config(val host: String, val port: Int, val queue: String, val manager: String, val channel: String)

    interface SendConverter : Function<Pair<MqTester.Message, Session>, Message>
    interface ReceiveConverter : Function<Message, MqTester.Message>
    companion object : KLogging() {
        @JvmField
        val DEFAULT_SEND_CONVERTER: SendConverter = object : SendConverter {
            override fun apply(m: Pair<MqTester.Message, Session>): Message = m.second.createTextMessage(m.first.body)
        }
        @JvmField
        val DEFAULT_RECEIVE_CONVERTER: ReceiveConverter = object : ReceiveConverter {
            override fun apply(m: Message): MqTester.Message = MqTester.Message((m as TextMessage).text)
        }
    }
}
