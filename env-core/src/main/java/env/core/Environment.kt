package env.core

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

open class Environment(val operators: Map<String, Operator<*>>) {

    init {
        logger.info("Environment settings:\nOperators: $operators\nConfig: $config")
    }

    @Suppress("SpreadOperator")
    fun up() {
        if (config.startEnv) {
            try {
                val elapsed = measureTimeMillis { allOf(*start(operators.entries))[config.upTimeout, SECONDS] }
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
            allOf(*operators.values.map { runAsync { it.stop() } }.toTypedArray())[config.downTimeout, SECONDS]
        }
    }

    fun down(vararg systems: String) {
        exec(systems, "Stopping {}...", config.downTimeout) { it.stop() }
    }

    @Suppress("SpreadOperator")
    private fun exec(systems: Array<out String>, logDesc: String, timeout: Long, operation: (Operator<*>) -> Unit) {
        allOf(
            *this.operators.entries
                .filter { systems.any { s: String -> it.key.toLowerCase().startsWith(s.toLowerCase()) } }
                .onEach { logger.info(logDesc, it.key) }
                .map { it.value }
                .map { runAsync { operation(it) } }
                .toTypedArray()
        ).thenRun { logger.info("Done. ${status()}") }[timeout, SECONDS]
    }

    fun status() =
        "Status:\n${operators.entries.joinToString("\n") { "${it.key}: ${if (it.value.running()) "up" else "down"}" }}"

    private fun summary() = "${javaClass.simpleName}\n\n ======= Test environment started {} ms =======\n\n" +
        operators.entries.joinToString("\n") { "${it.key}: ${it.value.describe()}" } +
        "\n\n ==============================================\n\n"

    @Suppress("UNCHECKED_CAST")
    fun <T> find(name: String): T = (operators[name] ?: error("System $name not found")).system() as T

    companion object : KLogging() {
        private val config = Config()

        private fun start(operators: Set<Map.Entry<String, Operator<*>>>): Array<CompletableFuture<*>> = operators
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

    @Suppress("MagicNumber")
    data class Config(
        val downTimeout: Long = "SPECS_ENV_DOWN_TIMEOUT_SEC".fromPropertyOrElse(10L),
        val upTimeout: Long = "SPECS_ENV_UP_TIMEOUT_SEC".fromPropertyOrElse(300L),
        val startEnv: Boolean = "SPECS_ENV_START".fromPropertyOrElse(true),
    )

    class StartupFail(t: Throwable) : RuntimeException(t)

    data class Prop(val name: String, val value: String) {
        fun pair() = name to value

        companion object {
            infix fun String.set(value: String) = Prop(this, value)
        }
    }
}

private class NamedThreadFactory(baseName: String) : ThreadFactory {
    private val threadsNum = AtomicInteger()
    private val namePattern: String = "$baseName-%d"
    override fun newThread(runnable: Runnable) = Thread(runnable, String.format(namePattern, threadsNum.addAndGet(1)))
}
