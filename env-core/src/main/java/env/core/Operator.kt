package env.core

import java.util.function.Consumer
import java.util.function.Function

/**
 * Object responsible for managing some underlying system
 */
interface Operator<T> {
    fun start()
    fun stop()
    fun running(): Boolean
    fun system(): T
    fun configure(consumer: Consumer<T>): Operator<T>? {
        consumer.accept(system())
        return this
    }

    companion object {
        fun <T> generic(system: T, start: Consumer<T>, stop: Consumer<T>, running: Function<T, Boolean>) =
            GenericOperator(system, start, stop, running)
    }
}

open class GenericOperator<T>(
    private val system: T,
    private val start: Consumer<T>,
    private val stop: Consumer<T>,
    private val running: Function<T, Boolean>
) : Operator<T> {
    override fun start() {
        start.accept(system)
    }

    override fun stop() {
        stop.accept(system)
    }

    override fun running(): Boolean = running.apply(system)

    override fun system(): T = system
}
