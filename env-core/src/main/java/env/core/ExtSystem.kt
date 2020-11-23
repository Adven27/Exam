package env.core

import org.testcontainers.containers.GenericContainer
import java.util.function.Consumer
import java.util.function.Function

interface ExtSystem<T> {
    fun start()
    fun stop()
    fun running(): Boolean
    fun system(): T
    fun configure(consumer: Consumer<T>): ExtSystem<T>? {
        consumer.accept(system())
        return this
    }
}

open class GenericExtSystem<T>(
    private val system: T,
    private val start: Consumer<T>,
    private val stop: Consumer<T>,
    private val running: Function<T, Boolean>
) : ExtSystem<T> {
    override fun start() {
        start.accept(system)
    }

    override fun stop() {
        stop.accept(system)
    }

    override fun running(): Boolean {
        return running.apply(system)
    }

    override fun system(): T {
        return system
    }
}

class ContainerizedSystem<T : GenericContainer<*>?>(system: T) : GenericExtSystem<T>(
    system,
    start = { it!!.start() },
    stop = { it!!.stop() },
    running = { it!!.isRunning }
)
