package env.mq.redis

import com.adven.concordion.extensions.exam.mq.MqTester.NOOP
import env.core.Environment.Companion.setProperties
import env.core.Environment.Prop
import env.core.Environment.Prop.Companion.set
import env.core.ExternalSystem
import env.core.PortsExposingStrategy
import env.core.PortsExposingStrategy.SystemPropertyToggle
import mu.KLogging
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName
import redis.clients.jedis.Jedis
import java.time.Duration.ofSeconds

class RedisContainerSystem @JvmOverloads constructor(
    dockerImageName: DockerImageName = DockerImageName.parse(IMAGE),
    portsExposingStrategy: PortsExposingStrategy = SystemPropertyToggle(),
    fixedPort: Int = PORT,
    private var config: Config = Config(),
    private val afterStart: RedisContainerSystem.() -> Unit = { }
) : GenericContainer<Nothing>(dockerImageName), ExternalSystem {
    private val fixedPort: Int

    init {
        withExposedPorts(PORT)
        withStartupTimeout(ofSeconds(STARTUP_TIMEOUT))
        this.fixedPort = fixedPort
        if (portsExposingStrategy.fixedPorts()) {
            addFixedExposedPort(fixedPort, PORT)
        }
    }

    override fun start() {
        super.start()
        config = Config(config.host.name set host, config.port.name set firstMappedPort.toString())
        apply(afterStart)
    }

    fun config() = config

    data class Config @JvmOverloads constructor(
        val host: Prop = "env.mq.redis.host" set "localhost",
        val port: Prop = "env.mq.redis.port" set PORT.toString()
    ) {
        init {
            mapOf(host.pair(), port.pair()).setProperties()
        }
    }

    open class RedisTester(private val port: Int) : NOOP() {
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

    override fun running() = isRunning
}
