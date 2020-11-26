package env.mq.ibmmq

import com.ibm.mq.jms.MQConnectionFactory
import com.ibm.msg.client.wmq.WMQConstants.WMQ_CM_CLIENT
import env.core.Environment.Companion.setProperties
import env.core.GenericOperator
import mu.KLogging
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.output.Slf4jLogConsumer
import org.testcontainers.containers.wait.strategy.Wait.forLogMessage
import org.testcontainers.utility.DockerImageName
import java.time.Duration.ofSeconds
import javax.jms.Session
import javax.jms.Session.AUTO_ACKNOWLEDGE

@Suppress("unused", "MagicNumber")
class EnvAwareIbmMQContainer @JvmOverloads constructor(
    dockerImageName: DockerImageName = DockerImageName.parse(IMAGE),
    fixedEnv: Boolean = false,
    fixedPort: Int = PORT,
    fixedPortAdm: Int = PORT_ADM,
    private val afterStart: EnvAwareIbmMQContainer.() -> Unit = { },
) : GenericContainer<Nothing>(dockerImageName) {
    private var config: IbmMqConfig = IbmMqConfig()

    init {
        withEnv("MQ_QMGR_NAME", "QM1")
        withEnv("LICENSE", "accept")
        withExposedPorts(PORT, PORT_ADM)
        waitingFor(
            forLogMessage(".*The queue manager task 'AUTOCONFIG' has ended.*", 1).withStartupTimeout(ofSeconds(120))
        )
        withLogConsumer(Slf4jLogConsumer(logger).withPrefix("IBMMQ"))
        if (fixedEnv) {
            addFixedExposedPort(fixedPort, PORT)
            addFixedExposedPort(fixedPortAdm, PORT_ADM)
        }
    }

    override fun start() {
        super.start()
        config = IbmMqConfig(port = getMappedPort(PORT)).apply { toSystemProperties() }
        apply(afterStart)
    }

    fun config() = config

    companion object : KLogging() {
        private const val PORT = 1414
        private const val PORT_ADM = 9443
        private const val IMAGE = "ibmcom/mq:latest"
    }
}

/**
 * Operator connects to the remote mq on start and creates 2 temporary queue.
 * Removing of this temporary queues is a responsibility of the remote queue broker.
 */
@Suppress("unused")
open class RemoteIbmMqOperator(
    private val host: String,
    private val port: Int,
    private val queueManager: String,
    private val channel: String,
) : GenericOperator<RemoteMq>(
    system = RemoteMq(),
    start = {
        it.config = MQConnectionFactory().apply {
            this.transportType = WMQ_CM_CLIENT
            this.hostName = host
            this.port = port
            this.queueManager = queueManager
            this.channel = channel
        }.createConnection().createSession(false, AUTO_ACKNOWLEDGE).let { session ->
            IbmMqConfig(host, port, session.tempQueue(), session.tempQueue(), queueManager, channel)
                .apply { toSystemProperties() }
        }
    },
    stop = {},
    running = { true }
)

data class RemoteMq(var config: IbmMqConfig = IbmMqConfig())

/**
 * Config based on default IBM MQ container configuration.
 * @see <a href="http://github.com/ibm-messaging/mq-container/blob/master/docs/developer-config.md">http://github.com/ibm-messaging</a>
 */
data class IbmMqConfig(
    val host: String = "localhost",
    val port: Int = 1414,
    val devQueue1: String = "DEV.QUEUE.1",
    val devQueue2: String = "DEV.QUEUE.2",
    val manager: String = "QM1",
    val channel: String = "DEV.APP.SVRCONN",
) {
    val jmsTester1 = JmsTester.Config(host, port, devQueue1, manager, channel)
    val jmsTester2 = JmsTester.Config(host, port, devQueue2, manager, channel)
    fun toSystemProperties() {
        mapOf(
            "env.mq.ibm.host" to host,
            "env.mq.ibm.port" to port.toString(),
            "env.mq.ibm.manager" to manager,
            "env.mq.ibm.channel" to channel,
            "env.mq.ibm.devQueue1" to devQueue1,
            "env.mq.ibm.devQueue2" to devQueue2
        ).setProperties()
    }

    companion object : KLogging()
}

private fun Session.tempQueue() = this.createTemporaryQueue().queueName
