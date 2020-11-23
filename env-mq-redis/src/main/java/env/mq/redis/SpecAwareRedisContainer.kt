package env.mq.redis

import com.adven.concordion.extensions.exam.mq.MqTester
import com.adven.concordion.extensions.exam.mq.MqTester.NOOP
import env.core.ContainerizedSystem
import env.core.ExtSystem
import mu.KLogging
import org.slf4j.LoggerFactory
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.output.Slf4jLogConsumer
import redis.clients.jedis.Jedis
import java.time.Duration.ofSeconds

class SpecAwareRedisContainer @JvmOverloads constructor(
    dockerImageName: String = IMAGE,
    fixedPort: Int = PORT,
    fixedEnv: Boolean
) : GenericContainer<Nothing>(dockerImageName) {
    private val fixedPort: Int
    override fun start() {
        super.start()
        System.setProperty(SYS_PROP_PORT, firstMappedPort.toString())
            .also { logger.info("System property set: $SYS_PROP_PORT = ${System.getProperty(SYS_PROP_PORT)} ") }
    }

    fun mqTester(): MqTester {
        val tester: MqTester = RedisTester(port())
        tester.start()
        return tester
    }

    fun port(): Int {
        return if (isRunning) firstMappedPort else fixedPort
    }

    internal class RedisTester(private val port: Int) : NOOP() {
        override fun start() {
            jedis = Jedis("localhost", port)
        }

        override fun send(message: String, headers: Map<String, String>) {
            val kv = message.split("=").toTypedArray()
            jedis!![kv[0].trim { it <= ' ' }] = kv[1].trim { it <= ' ' }
        }

        override fun stop() {
            jedis!!.close()
        }

        companion object {
            private var jedis: Jedis? = null
        }
    }

    init {
        withExposedPorts(PORT)
        withLogConsumer(Slf4jLogConsumer(LoggerFactory.getLogger("REDIS")))
        withStartupTimeout(ofSeconds(STARTUP_TIMEOUT))
        this.fixedPort = fixedPort
        if (fixedEnv) {
            addFixedExposedPort(fixedPort, PORT)
        }
    }

    companion object : KLogging() {
        const val SYS_PROP_PORT = "env.mq.redis.port"
        private const val PORT = 6379
        private const val IMAGE = "redis:5.0.3-alpine"
        private const val STARTUP_TIMEOUT = 30L

        @JvmOverloads
        fun system(
            dockerImageName: String = IMAGE,
            fixedPort: Int = PORT,
            fixedEnv: Boolean = false
        ): ExtSystem<SpecAwareRedisContainer> =
            ContainerizedSystem(SpecAwareRedisContainer(dockerImageName, fixedPort, fixedEnv))
    }
}
