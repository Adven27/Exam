package env.core

import java.util.function.Consumer
import java.util.function.Function

/**
 * Object responsible for managing some underlying system
 */
interface ExternalSystem {
    fun start()
    fun stop()
    fun running(): Boolean
    fun describe(): String = toString()

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
        ) = GenericExternalSystem(system, start, stop, running)
    }
}

open class GenericExternalSystem<T> @JvmOverloads constructor(
    val system: T,
    private val start: Consumer<T> = Consumer {},
    private val stop: Consumer<T> = Consumer {},
    private val running: Function<T, Boolean> = Function { true }
) : ExternalSystem {
    override fun start() {
        start.accept(system)
    }

    override fun stop() {
        stop.accept(system)
    }

    override fun running(): Boolean = running.apply(system)
    override fun describe() = system.toString()
}

interface PortsExposingStrategy {

    fun fixedPorts(): Boolean

    class SystemPropertyToggle @JvmOverloads constructor(
        private val property: String = "SPECS_ENV_FIXED",
        private val orElse: Boolean = false
    ) : PortsExposingStrategy {
        override fun fixedPorts(): Boolean = System.getProperty(property, orElse.toString()).toBoolean()
    }
}
