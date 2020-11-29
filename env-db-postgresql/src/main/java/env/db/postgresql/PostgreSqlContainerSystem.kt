package env.db.postgresql

import env.container.parseImage
import env.core.Environment.Companion.setProperties
import env.core.Environment.Prop
import env.core.Environment.Prop.Companion.set
import env.core.ExternalSystem
import env.core.PortsExposingStrategy
import env.core.PortsExposingStrategy.SystemPropertyToggle
import mu.KLogging
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

@Suppress("LongParameterList")
class PostgreSqlContainerSystem @JvmOverloads constructor(
    dockerImageName: DockerImageName = "postgres:9.6.12".parseImage(),
    portsExposingStrategy: PortsExposingStrategy = SystemPropertyToggle(),
    fixedPort: Int = POSTGRESQL_PORT,
    private var config: Config = Config(),
    private val afterStart: PostgreSqlContainerSystem.() -> Unit = { }
) : PostgreSQLContainer<Nothing>(dockerImageName), ExternalSystem {

    init {
        if (portsExposingStrategy.fixedPorts()) {
            addFixedExposedPort(fixedPort, POSTGRESQL_PORT)
        }
    }

    override fun start() {
        super.start()
        config = config.refreshValues()
        apply(afterStart)
    }

    private fun Config.refreshValues() = Config(
        jdbcUrl.name set getJdbcUrl(),
        username.name set getUsername(),
        password.name set getPassword(),
        driver.name set driverClassName
    )

    override fun running() = isRunning

    fun config() = config

    data class Config @JvmOverloads constructor(
        var jdbcUrl: Prop = PROP_URL set "jdbc:postgresql://localhost:$POSTGRESQL_PORT/postgres?stringtype=unspecified",
        var username: Prop = PROP_USER set "test",
        var password: Prop = PROP_PASSWORD set "test",
        var driver: Prop = PROP_DRIVER set "org.postgresql.Driver"
    ) {
        init {
            mapOf(jdbcUrl.pair(), username.pair(), password.pair(), driver.pair()).setProperties()
        }

        constructor(url: String, username: String, password: String) : this(
            PROP_URL set url,
            PROP_USER set username,
            PROP_PASSWORD set password
        )
    }

    companion object : KLogging() {
        const val PROP_URL = "env.db.postgresql.url"
        const val PROP_USER = "env.db.postgresql.username"
        const val PROP_PASSWORD = "env.db.postgresql.password"
        const val PROP_DRIVER = "env.db.postgresql.driver"
    }
}
