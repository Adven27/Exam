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
import javax.jms.MessageProducer
import javax.jms.Queue
import javax.jms.QueueBrowser
import javax.jms.Session
import javax.jms.TextMessage

@Suppress("unused")
open class IbmMQReadWriteTester(config: Config) : IbmMQReadOnlyTester(config) {
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

open class IbmMQReadOnlyTester(config: Config) : JmsTester(config) {
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
        browser = session.createBrowser(queue)
    }
}

abstract class JmsTester(protected val config: Config) : MqTester.NOOP() {
    companion object : KLogging()

    protected lateinit var session: Session

    override fun start() {
        with(connectionFactory().createConnection()) {
            session = createSession(false, Session.AUTO_ACKNOWLEDGE)
            config.queue.let { q -> session.createQueue(queueName(q)).also { afterQueueCreation(it) } }
            start()
        }
        logger.info("MqTester started with config $config")
    }

    private fun queueName(q: String) = if (q.startsWith("queue://")) q else "queue:///$q"

    override fun stop() {
        session.close()
    }

    private fun connectionFactory() = JmsFactoryFactory.getInstance(WMQ_PROVIDER).createConnectionFactory().apply {
        setStringProperty(WMQ_HOST_NAME, config.host)
        setIntProperty(WMQ_PORT, config.port)
        setStringProperty(WMQ_CHANNEL, config.channel)
        setStringProperty(WMQ_QUEUE_MANAGER, config.manager)
        setIntProperty(WMQ_CONNECTION_MODE, WMQ_CM_CLIENT)
    }

    abstract fun afterQueueCreation(queue: Queue)

    data class Config(val host: String, val port: Int, val queue: String, val manager: String, val channel: String)
}
