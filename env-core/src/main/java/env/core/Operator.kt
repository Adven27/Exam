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
    fun describe(): String
    fun configure(consumer: Consumer<T>): Operator<T>? {
        consumer.accept(system())
        return this
    }

    companion object {
        /*
        TODO https://youtrack.jetbrains.com/issue/KT-35716
        @JvmStatic
        @JvmOverloads
        */
        fun <T> generic(
            system: T,
            start: Consumer<T> = Consumer {},
            stop: Consumer<T> = Consumer {},
            running: Function<T, Boolean> = Function { true }
        ) = GenericOperator(system, start, stop, running)
    }
}

open class GenericOperator<T> @JvmOverloads constructor(
    private val system: T,
    private val start: Consumer<T> = Consumer {},
    private val stop: Consumer<T> = Consumer {},
    private val running: Function<T, Boolean> = Function { true }
) : Operator<T> {
    override fun start() {
        start.accept(system)
    }

    override fun stop() {
        stop.accept(system)
    }

    override fun running(): Boolean = running.apply(system)
    override fun system(): T = system
    override fun describe() = system.toString()
}
