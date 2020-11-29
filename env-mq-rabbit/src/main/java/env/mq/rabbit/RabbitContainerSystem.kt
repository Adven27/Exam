package env.mq.rabbit

import env.core.Environment.Companion.setProperties
import env.core.Environment.Prop
import env.core.Environment.Prop.Companion.set
import env.core.ExternalSystem
import env.core.PortsExposingStrategy
import env.core.PortsExposingStrategy.SystemPropertyToggle
import mu.KLogging
import org.testcontainers.containers.RabbitMQContainer
import org.testcontainers.utility.DockerImageName

open class RabbitContainerSystem @JvmOverloads constructor(
    dockerImageName: DockerImageName = DockerImageName.parse("rabbitmq:3.7.25-management-alpine"),
    portsExposingStrategy: PortsExposingStrategy = SystemPropertyToggle(),
    fixedPort: Int = PORT,
    fixedPortAdm: Int = PORT_ADM,
    private var config: Config = Config(),
    private val afterStart: RabbitContainerSystem.() -> Unit = { }
) : RabbitMQContainer(dockerImageName), ExternalSystem {

    init {
        if (portsExposingStrategy.fixedPorts()) {
            addFixedExposedPort(fixedPort, PORT)
            addFixedExposedPort(fixedPortAdm, PORT_ADM)
        }
    }

    override fun start() {
        super.start()
        config = Config(config.host.name set host, config.port.name set amqpPort.toString())
        apply(afterStart)
    }

    override fun running() = isRunning

    @Suppress("unused")
    fun config(): Config = config

    data class Config constructor(
        val host: Prop = PROP_HOST set "localhost",
        val port: Prop = PROP_PORT set PORT.toString()
    ) {
        init {
            mapOf(host.pair(), port.pair()).setProperties()
        }
    }

    companion object : KLogging() {
        private const val PORT = 5672
        private const val PORT_ADM = 15672
        const val PROP_HOST = "env.mq.rabbit.host"
        const val PROP_PORT = "env.mq.rabbit.port"
    }
}
