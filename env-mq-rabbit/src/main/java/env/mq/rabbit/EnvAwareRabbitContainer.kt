package env.mq.rabbit

import env.core.Environment.Companion.setProperties
import mu.KLogging
import org.testcontainers.containers.RabbitMQContainer
import org.testcontainers.utility.DockerImageName

open class EnvAwareRabbitContainer @JvmOverloads constructor(
    dockerImageName: DockerImageName = DockerImageName.parse("rabbitmq:3.7.25-management-alpine"),
    fixedEnv: Boolean = false,
    private val fixedPort: Int = PORT,
    fixedPortAdm: Int = PORT_ADM,
    val portSystemPropertyName: String = "env.mq.rabbit.port",
    private val afterStart: EnvAwareRabbitContainer.() -> Unit = { }
) : RabbitMQContainer(dockerImageName) {

    init {
        if (fixedEnv) {
            addFixedExposedPort(fixedPort, PORT)
            addFixedExposedPort(fixedPortAdm, PORT_ADM)
        }
    }

    override fun start() {
        super.start()
        mapOf(portSystemPropertyName to firstMappedPort.toString()).setProperties()
        apply(afterStart)
    }

    @Suppress("unused")
    fun port(): Int = if (isRunning) amqpPort else fixedPort

    companion object : KLogging() {
        private const val PORT = 5672
        private const val PORT_ADM = 15672
    }
}
