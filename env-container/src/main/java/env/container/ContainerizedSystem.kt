package env.container

import env.core.GenericExtSystem
import org.testcontainers.containers.GenericContainer

open class ContainerizedSystem<T : GenericContainer<*>?>(system: T) : GenericExtSystem<T>(
    system,
    start = { it!!.start() },
    stop = { it!!.stop() },
    running = { it!!.isRunning }
)
