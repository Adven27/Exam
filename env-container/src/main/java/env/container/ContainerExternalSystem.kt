package env.container

import env.core.GenericExternalSystem
import org.testcontainers.containers.GenericContainer

/**
 * System implementation based on docker container
 */
@Suppress("unused")
open class ContainerExternalSystem<T : GenericContainer<*>>(system: T) : GenericExternalSystem<T>(
    system,
    start = { it.start() },
    stop = { it.stop() },
    running = { it.isRunning }
)
