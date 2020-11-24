package env.core

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

    companion object {
        fun <T> generic(system: T, start: Consumer<T>, stop: Consumer<T>, running: Function<T, Boolean>) =
            GenericExtSystem(system, start, stop, running)
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

    override fun running(): Boolean = running.apply(system)

    override fun system(): T = system
}
