package env.container

import env.core.GenericOperator
import org.testcontainers.containers.GenericContainer

/**
 * Operator implementation responsible for managing docker container
 */
open class ContainerOperator<T : GenericContainer<*>?>(system: T) : GenericOperator<T>(
    system,
    start = { it!!.start() },
    stop = { it!!.stop() },
    running = { it!!.isRunning }
)
