package env.mq.ibmmq

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
open class IbmMQBrowseAndSendTester(config: Config) : IbmMQBrowseOnlyTester(config) {
    companion object : KLogging()

    private lateinit var producer: MessageProducer

    override fun send(message: String, headers: Map<String, String>) {
        logger.info("Sending to {}...", config.queue)
        producer.send(session.createTextMessage(message).apply { jmsCorrelationID = headers["jmsCorrelationID"] })
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
open class IbmMQConsumeAndSendTester(config: Config) : IbmMQConsumeOnlyTester(config) {
    companion object : KLogging()

    private lateinit var producer: MessageProducer

    override fun send(message: String, headers: Map<String, String>) {
        logger.info("Sending to {}...", config.queue)
        producer.send(session.createTextMessage(message).apply { jmsCorrelationID = headers["jmsCorrelationID"] })
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
open class IbmMQBrowseOnlyTester(config: Config) : JmsTester(config) {
    companion object : KLogging()

    private lateinit var browser: QueueBrowser

    override fun receive(): List<MqTester.Message> {
        logger.info("Reading from {}", config.queue)
        val result: MutableList<MqTester.Message> = mutableListOf()
        val messages = browser.enumeration
        while (messages.hasMoreElements()) {
            result.add(messages.nextElement().let { MqTester.Message((it as TextMessage).text) })
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
open class IbmMQConsumeOnlyTester(config: Config) : JmsTester(config) {
    companion object : KLogging()

    override fun receive(): List<MqTester.Message> {
        logger.info("Reading from {}", config.queue)
        val result: MutableList<MqTester.Message> = mutableListOf()
        while (true) {
            val message = consumer.receiveNoWait() ?: break
            result.add(message.let { MqTester.Message((it as TextMessage).text) })
        }
        return result
    }
}

abstract class JmsTester(protected val config: Config) : MqTester.NOOP() {
    companion object : KLogging()

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
}
