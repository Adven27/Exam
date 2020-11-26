package env.db.postgresql

import com.adven.concordion.extensions.exam.db.DbTester
import env.core.Environment.Companion.setProperties
import mu.KLogging
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

@Suppress("LongParameterList")
class EnvAwarePostgreSqlContainer @JvmOverloads constructor(
    dockerImageName: DockerImageName = DockerImageName.parse("postgres:9.6.12"),
    fixedEnv: Boolean = false,
    fixedPort: Int = POSTGRESQL_PORT,
    private val urlSystemPropertyName: String = "env.db.postgresql.url",
    private val usernameSystemPropertyName: String = "env.db.postgresql.username",
    private val passwordSystemPropertyName: String = "env.db.postgresql.password",
    private val driverSystemPropertyName: String = "env.db.postgresql.driver",
    private val afterStart: EnvAwarePostgreSqlContainer.() -> Unit = { }
) : PostgreSQLContainer<Nothing>(dockerImageName) {

    init {
        if (fixedEnv) {
            addFixedExposedPort(fixedPort, POSTGRESQL_PORT)
        }
    }

    override fun start() {
        super.start()
        mapOf(
            urlSystemPropertyName to jdbcUrl,
            usernameSystemPropertyName to username,
            passwordSystemPropertyName to password,
            driverSystemPropertyName to driverClassName,
        ).setProperties()
        apply(afterStart)
    }

    @JvmOverloads
    fun dbTester(port: Int = POSTGRESQL_PORT): DbTester =
        dbTester("jdbc:postgresql://localhost:$port/postgres?stringtype=unspecified", "test", "test")

    fun dbTester(fixedUrl: String, fixedUser: String, fixedPassword: String): DbTester {
        val running = isRunning
        return DbTester(
            "org.postgresql.Driver",
            (if (running) jdbcUrl else fixedUrl)!!,
            (if (running) username else fixedUser)!!,
            (if (running) password else fixedPassword)!!,
            null
        )
    }

    companion object : KLogging()
}
