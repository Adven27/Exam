package env.mq.rabbit

import env.core.ContainerizedSystem
import env.core.ExtSystem
import org.testcontainers.containers.RabbitMQContainer

open class SpecAwareRabbitContainer @JvmOverloads constructor(
    private val fixedPort: Int = PORT,
    private val fixedPortAdm: Int = PORT_ADM,
    fixedEnv: Boolean = false
) : RabbitMQContainer() {
    override fun start() {
        super.start()
        System.setProperty("specs-mq-port", firstMappedPort.toString())
    }

    fun port() = if (isRunning) firstMappedPort else fixedPort

    init {
        if (fixedEnv) {
            addFixedExposedPort(fixedPort, PORT)
            addFixedExposedPort(fixedPortAdm, PORT_ADM)
        }
    }

    companion object {
        private const val PORT = 5672
        private const val PORT_ADM = 15672

        @JvmOverloads
        fun system(
            fixedPort: Int = PORT,
            fixedPortAdm: Int = PORT_ADM,
            fixedEnv: Boolean = false
        ): ExtSystem<SpecAwareRabbitContainer> =
            ContainerizedSystem(SpecAwareRabbitContainer(fixedPort, fixedPortAdm, fixedEnv))
    }
}
