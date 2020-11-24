package env.core

import mu.KLogging
import java.lang.System.currentTimeMillis
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.runAsync
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit.SECONDS
import java.util.concurrent.atomic.AtomicInteger

open class Environment(val systems: Map<String, ExtSystem<*>>) {

    init {
        logger.info("Environment settings:\nSystems: $systems\nConfig: $config")
    }

    @Suppress("SpreadOperator")
    fun up() {
        val start = currentTimeMillis()
        CompletableFuture.allOf(*start(systems.entries))
            .thenRun { logger.info(summary(), currentTimeMillis() - start) }[config.upTimeout, SECONDS]
    }

    private fun summary() = "${javaClass.simpleName}\n\n ======= Test environment started {} ms =======\n\n" +
        systems.entries.joinToString("\n") { "${it.key}: ${it.value.system()}" } +
        "\n\n ==============================================\n\n"

    @Suppress("SpreadOperator")
    fun down() {
        CompletableFuture.allOf(
            *systems.values.map { runAsync { it.stop() } }.toTypedArray()
        )[config.downTimeout, SECONDS]
    }

    @Suppress("SpreadOperator")
    fun down(vararg systems: String) {
        CompletableFuture.allOf(
            *this.systems.entries
                .filter { systems.any { s: String -> it.key.toLowerCase().startsWith(s.toLowerCase()) } }
                .onEach { logger.info("Stopping {}...", it.key) }
                .map { it.value }
                .map { runAsync { it.stop() } }
                .toTypedArray()
        ).thenRun { logger.info(status()) }[config.downTimeout, SECONDS]
    }

    private fun status() =
        "Done. Status:\n${systems.entries.joinToString("\n") { "${it.key}: ${if (it.value.running()) "up" else "down"}" }}"

    @Suppress("SpreadOperator")
    fun up(vararg systems: String) {
        CompletableFuture.allOf(
            *this.systems.entries
                .filter { systems.any { s: String -> it.key.toLowerCase().startsWith(s.toLowerCase()) } }
                .onEach { sys: Map.Entry<String, ExtSystem<*>> -> logger.info("Starting {}...", sys.key) }
                .map { it.value }
                .map { runAsync { it.start() } }
                .toTypedArray()
        ).thenRun { logger.info(status()) }[config.upTimeout, SECONDS]
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> find(name: String): T = (systems[name] ?: error("System $name not found")).system() as T

    companion object : KLogging() {
        private val config = Config()

        private fun start(systems: Set<Map.Entry<String, ExtSystem<*>>>): Array<CompletableFuture<*>> = systems
            .filter { config.startEnv }
            .onEach { logger.info("Preparing to start {}", it.key) }
            .map { runAsync({ it.value.start() }, Executors.newCachedThreadPool(NamedThreadFactory(it.key))) }
            .toTypedArray()

        @JvmStatic
        fun findAvailableTcpPort(): Int = SocketUtils.findAvailableTcpPort()
    }

    data class Config(
        val downTimeout: Long = System.getProperty("SPECS_ENV_DOWN_TIMEOUT_SEC", "10").toLong(),
        val upTimeout: Long = System.getProperty("SPECS_ENV_UP_TIMEOUT_SEC", "300").toLong(),
        val startEnv: Boolean = System.getProperty("SPECS_ENV_START", "true").toBoolean(),
        val fixedEnv: Boolean = System.getProperty("SPECS_ENV_FIXED", "false").toBoolean()
    )
}

class NamedThreadFactory(baseName: String) : ThreadFactory {
    private val threadsNum = AtomicInteger()
    private val namePattern: String = "$baseName-%d"
    override fun newThread(runnable: Runnable) = Thread(runnable, String.format(namePattern, threadsNum.addAndGet(1)))
}
