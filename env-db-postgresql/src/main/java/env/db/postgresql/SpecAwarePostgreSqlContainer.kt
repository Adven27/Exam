package env.db.postgresql

import com.adven.concordion.extensions.exam.db.DbTester
import env.core.ContainerizedSystem
import env.core.ExtSystem
import mu.KLogging
import org.testcontainers.containers.PostgreSQLContainer

class SpecAwarePostgreSqlContainer @JvmOverloads constructor(
    dockerImageName: String,
    fixedPort: Int = POSTGRESQL_PORT,
    fixedEnv: Boolean = false
) : PostgreSQLContainer<Nothing>(dockerImageName) {
    override fun start() {
        super.start()
        System.setProperty(SYS_PROP_URL, jdbcUrl)
            .also { logger.info("System property set: $SYS_PROP_URL = ${System.getProperty(SYS_PROP_URL)} ") }
    }

    @JvmOverloads
    fun dbTester(port: Int = POSTGRESQL_PORT): DbTester =
        dbTester("jdbc:postgresql://localhost:$port/postgres?stringtype=unspecified", "test", "test", "test")

    fun dbTester(fixedUrl: String?, fixedUser: String?, fixedPassword: String?, fixedDbName: String?): DbTester {
        val running = isRunning
        return DbTester(
            "org.postgresql.Driver",
            (if (running) jdbcUrl else fixedUrl)!!,
            (if (running) username else fixedUser)!!,
            (if (running) password else fixedPassword)!!,
            if (running) databaseName else fixedDbName
        )
    }

    init {
        if (fixedEnv) {
            addFixedExposedPort(fixedPort, POSTGRESQL_PORT)
        }
    }

    companion object : KLogging() {
        const val SYS_PROP_URL = "env.db.postgresql.url"

        @JvmOverloads
        fun system(
            dockerImageName: String,
            fixedPort: Int = POSTGRESQL_PORT,
            fixedEnv: Boolean = false
        ): ExtSystem<SpecAwarePostgreSqlContainer> =
            ContainerizedSystem(SpecAwarePostgreSqlContainer(dockerImageName, fixedPort, fixedEnv))
    }
}
