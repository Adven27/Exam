package env.mq.redis

import com.adven.concordion.extensions.exam.mq.MqTester
import com.adven.concordion.extensions.exam.mq.MqTester.NOOP
import env.core.Environment.Companion.setProperties
import mu.KLogging
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.output.Slf4jLogConsumer
import org.testcontainers.utility.DockerImageName
import redis.clients.jedis.Jedis
import java.time.Duration.ofSeconds

class EnvAwareRedisContainer @JvmOverloads constructor(
    dockerImageName: DockerImageName = DockerImageName.parse(IMAGE),
    fixedEnv: Boolean = false,
    fixedPort: Int = PORT,
    val portSystemPropertyName: String = "env.mq.redis.port",
    private val afterStart: EnvAwareRedisContainer.() -> Unit = { }
) : GenericContainer<Nothing>(dockerImageName) {
    private val fixedPort: Int

    init {
        withExposedPorts(PORT)
        withLogConsumer(Slf4jLogConsumer(logger).withPrefix("REDIS"))
        withStartupTimeout(ofSeconds(STARTUP_TIMEOUT))
        this.fixedPort = fixedPort
        if (fixedEnv) {
            addFixedExposedPort(fixedPort, PORT)
        }
    }

    override fun start() {
        super.start()
        mapOf(portSystemPropertyName to firstMappedPort.toString()).setProperties()
        apply(afterStart)
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
            jedis[kv[0].trim { it <= ' ' }] = kv[1].trim { it <= ' ' }
        }

        override fun stop() {
            jedis.close()
        }

        companion object {
            private lateinit var jedis: Jedis
        }
    }

    companion object : KLogging() {
        private const val PORT = 6379
        private const val IMAGE = "redis:5.0.3-alpine"
        private const val STARTUP_TIMEOUT = 30L
    }
}
