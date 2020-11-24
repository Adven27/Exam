package env.db.postgresql

import com.adven.concordion.extensions.exam.db.DbTester
import mu.KLogging
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

class SpecAwarePostgreSqlContainer @JvmOverloads constructor(
    dockerImageName: DockerImageName = DockerImageName.parse("postgres:9.6.12"),
    fixedEnv: Boolean = false,
    fixedPort: Int = POSTGRESQL_PORT,
    val urlSystemPropertyName: String = "env.db.postgresql.url",
    private val afterStart: SpecAwarePostgreSqlContainer.() -> Unit = { }
) : PostgreSQLContainer<Nothing>(dockerImageName) {

    init {
        if (fixedEnv) {
            addFixedExposedPort(fixedPort, POSTGRESQL_PORT)
        }
    }

    override fun start() {
        super.start()
        System.setProperty(urlSystemPropertyName, jdbcUrl).also {
            logger.info("System property set: $urlSystemPropertyName = ${System.getProperty(urlSystemPropertyName)} ")
        }
        apply(afterStart)
    }

    @JvmOverloads
    fun dbTester(port: Int = POSTGRESQL_PORT): DbTester =
        dbTester("jdbc:postgresql://localhost:$port/postgres?stringtype=unspecified", "test", "test", "test")

    fun dbTester(fixedUrl: String, fixedUser: String, fixedPassword: String, fixedDbName: String): DbTester {
        val running = isRunning
        return DbTester(
            "org.postgresql.Driver",
            (if (running) jdbcUrl else fixedUrl)!!,
            (if (running) username else fixedUser)!!,
            (if (running) password else fixedPassword)!!,
            if (running) databaseName else fixedDbName
        )
    }

    companion object : KLogging()
}
