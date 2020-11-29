package env.core

import env.core.Environment.ConfigResolver.FromSystemProperty
import mu.KLogging
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.allOf
import java.util.concurrent.CompletableFuture.runAsync
import java.util.concurrent.Executors.newCachedThreadPool
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit.SECONDS
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.measureTimeMillis

open class Environment(val config: Config, val systems: Map<String, ExternalSystem>) {
    @JvmOverloads
    constructor(
        systems: Map<String, ExternalSystem>,
        configResolver: ConfigResolver = FromSystemProperty()
    ) : this(configResolver.resolve(), systems)

    init {
        logger.info("Environment settings:\nSystems: $systems\nConfig: $config")
    }

    @Suppress("SpreadOperator")
    fun up() {
        if (config.startEnv) {
            try {
                val elapsed = measureTimeMillis { allOf(*start(systems.entries))[config.upTimeout, SECONDS] }
                logger.info(summary(), elapsed)
            } catch (e: TimeoutException) {
                logger.error("Startup timeout exceeded: expected ${config.upTimeout}s. ${status()}", e)
                throw StartupFail(e)
            }
        } else {
            logger.info("Skip environment starting.")
        }
    }

    fun up(vararg systems: String) {
        exec(systems, "Starting {}...", config.upTimeout) { it.start() }
    }

    @Suppress("SpreadOperator")
    fun down() {
        if (config.startEnv) {
            allOf(*systems.values.map { runAsync { it.stop() } }.toTypedArray())[config.downTimeout, SECONDS]
        }
    }

    fun down(vararg systems: String) {
        exec(systems, "Stopping {}...", config.downTimeout) { it.stop() }
    }

    @Suppress("SpreadOperator")
    private fun exec(systems: Array<out String>, logDesc: String, timeout: Long, operation: (ExternalSystem) -> Unit) {
        allOf(
            *this.systems.entries
                .filter { systems.any { s: String -> it.key.toLowerCase().startsWith(s.toLowerCase()) } }
                .onEach { logger.info(logDesc, it.key) }
                .map { it.value }
                .map { runAsync { operation(it) } }
                .toTypedArray()
        ).thenRun { logger.info("Done. ${status()}") }[timeout, SECONDS]
    }

    fun status() =
        "Status:\n${systems.entries.joinToString("\n") { "${it.key}: ${if (it.value.running()) "up" else "down"}" }}"

    private fun summary() = "${javaClass.simpleName}\n\n ======= Test environment started {} ms =======\n\n" +
        systems.entries.joinToString("\n") { "${it.key}: ${it.value.describe()}" } +
        "\n\n ==============================================\n\n"

    @Suppress("UNCHECKED_CAST")
    fun <T : ExternalSystem> find(name: String): T = (systems[name] ?: error("System $name not found")) as T

    companion object : KLogging() {
        private fun start(systems: Set<Map.Entry<String, ExternalSystem>>): Array<CompletableFuture<*>> = systems
            .onEach { logger.info("Preparing to start {}", it.key) }
            .map { runAsync({ it.value.start() }, newCachedThreadPool(NamedThreadFactory(it.key))) }
            .toTypedArray()

        @JvmStatic
        fun findAvailableTcpPort(): Int = SocketUtils.findAvailableTcpPort()

        @JvmStatic
        fun String.fromPropertyOrElse(orElse: Long) = System.getProperty(this, orElse.toString()).toLong()

        @JvmStatic
        fun String.fromPropertyOrElse(orElse: Boolean) = System.getProperty(this, orElse.toString()).toBoolean()

        @JvmStatic
        fun Map<String, String>.setProperties() = this.forEach { (p, v) ->
            System.setProperty(p, v).also { logger.info("Set system property : $p = ${System.getProperty(p)}") }
        }
    }

    class StartupFail(t: Throwable) : RuntimeException(t)

    data class Prop(val name: String, val value: String) {
        fun pair() = name to value

        companion object {
            infix fun String.set(value: String) = Prop(this, value)
        }
    }

    data class Config(val downTimeout: Long, val upTimeout: Long, val startEnv: Boolean)

    interface ConfigResolver {
        fun resolve(): Config

        @Suppress("MagicNumber")
        class FromSystemProperty(
            private val startEnvProperty: String = "SPECS_ENV_START",
            private val upTimeoutProperty: String = "SPECS_ENV_UP_TIMEOUT_SEC",
            private val downTimeoutProperty: String = "SPECS_ENV_DOWN_TIMEOUT_SEC",
        ) : ConfigResolver {
            override fun resolve() = Config(
                startEnv = startEnvProperty.fromPropertyOrElse(true),
                upTimeout = upTimeoutProperty.fromPropertyOrElse(300L),
                downTimeout = downTimeoutProperty.fromPropertyOrElse(10L),
            )
        }
    }
}

private class NamedThreadFactory(baseName: String) : ThreadFactory {
    private val threadsNum = AtomicInteger()
    private val namePattern: String = "$baseName-%d"
    override fun newThread(runnable: Runnable) = Thread(runnable, String.format(namePattern, threadsNum.addAndGet(1)))
}
