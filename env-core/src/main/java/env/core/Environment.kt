package env.core

import org.slf4j.LoggerFactory
import org.testcontainers.shaded.com.google.common.util.concurrent.ThreadFactoryBuilder
import java.lang.System.currentTimeMillis
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.runAsync
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit.SECONDS

class Environment(private val sys: Map<String, ExtSystem<*>>) {

    init {
        log.info("Environment settings:\nSystems $sys\nConfig $config")
    }

    @Suppress("SpreadOperator")
    fun up() {
        val start = currentTimeMillis()
        CompletableFuture.allOf(*start(sys.entries))
            .thenRun { log.info(summary(), currentTimeMillis() - start) }[config.upTimeout, SECONDS]
    }

    private fun summary() = "${javaClass.simpleName}\n\n ======= Test environment started {} ms =======\n\n" +
        sys.entries.joinToString("\n") { "${it.key}: ${it.value.system()}" } +
        "\n\n ==============================================\n\n"

    @Suppress("SpreadOperator")
    fun down() {
        CompletableFuture.allOf(*sys.values.map { runAsync { it.stop() } }.toTypedArray())[config.downTimeout, SECONDS]
    }

    @Suppress("SpreadOperator")
    fun down(vararg systems: String) {
        CompletableFuture.allOf(
            *sys.entries
                .filter { systems.any { s: String -> it.key.toLowerCase().startsWith(s.toLowerCase()) } }
                .onEach { log.info("Stopping {}...", it.key) }
                .map { it.value }
                .map { runAsync { it.stop() } }
                .toTypedArray()
        ).thenRun { log.info(status()) }[config.downTimeout, SECONDS]
    }

    private fun status() =
        "Done. Status:\n${sys.entries.joinToString("\n") { "${it.key}: ${if (it.value.running()) "up" else "down"}" }}"

    @Suppress("SpreadOperator")
    fun up(vararg systems: String) {
        CompletableFuture.allOf(
            *sys.entries
                .filter { systems.any { s: String -> it.key.toLowerCase().startsWith(s.toLowerCase()) } }
                .onEach { sys: Map.Entry<String, ExtSystem<*>> -> log.info("Starting {}...", sys.key) }
                .map { it.value }
                .map { runAsync { it.start() } }
                .toTypedArray()
        ).thenRun { log.info(status()) }[config.upTimeout, SECONDS]
    }

    fun find(name: String): ExtSystem<*> = sys[name] ?: error("System $name not found")

    companion object {
        private val log = LoggerFactory.getLogger(Environment::class.java)
        private val config = Config()

        private fun start(systems: Set<Map.Entry<String, ExtSystem<*>>>): Array<CompletableFuture<*>> = systems
            .filter {
                ifRegularBuildThenStartAll() ||
                    ifGradleEnvRunThenStartOnlyContainers(it.value) ||
                    ifGradleSpecsRunThenSkip(it.value)
            }
            .onEach { log.info("Preparing to start {}", it.key) }
            .map { runAsync({ it.value.start() }, Executors.newCachedThreadPool(named(it.key))) }
            .toTypedArray()

        private fun ifRegularBuildThenStartAll() = config.startEnv && !config.fixedEnv
        private fun ifGradleEnvRunThenStartOnlyContainers(system: ExtSystem<*>) =
            config.startEnv && config.fixedEnv && container(system)

        private fun ifGradleSpecsRunThenSkip(system: ExtSystem<*>) = !config.startEnv && !container(system)
        private fun container(system: ExtSystem<*>) = system is ContainerizedSystem<*>
        private fun named(key: String) = ThreadFactoryBuilder().setNameFormat("$key-%d").build()
    }

    data class Config(
        val downTimeout: Long = System.getProperty("SPECS_ENV_DOWN_TIMEOUT_SEC", "10").toLong(),
        val upTimeout: Long = System.getProperty("SPECS_ENV_UP_TIMEOUT_SEC", "180").toLong(),
        val startEnv: Boolean = System.getProperty("SPECS_ENV_START", "true").toBoolean(),
        val fixedEnv: Boolean = System.getProperty("SPECS_ENV_FIXED", "false").toBoolean()
    )
}
