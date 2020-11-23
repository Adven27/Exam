package env.db.mysql

import com.adven.concordion.extensions.exam.db.DbTester
import env.core.ContainerizedSystem
import env.core.ExtSystem
import mu.KLogging
import org.testcontainers.containers.MySQLContainer

class SpecAwareMySqlContainer @JvmOverloads constructor(
    dockerImageName: String,
    fixedPort: Int = MYSQL_PORT,
    fixedEnv: Boolean = false
) : MySQLContainer<Nothing>(dockerImageName) {
    override fun start() {
        super.start()
        System.setProperty(SYS_PROP_URL, jdbcUrl)
            .also { logger.info("System property set: $SYS_PROP_URL = ${System.getProperty(SYS_PROP_URL)} ") }
    }

    @JvmOverloads
    fun dbTester(port: Int = MYSQL_PORT): DbTester {
        return dbTester("jdbc:mysql://localhost:$port/test?autoReconnect=true&useSSL=false", "test", "test", "test")
    }

    fun dbTester(fixedUrl: String?, fixedUser: String?, fixedPassword: String?, fixedDbName: String?): DbTester {
        val running = isRunning
        return DbTester(
            "com.mysql.cj.jdbc.Driver",
            (if (running) jdbcUrl else fixedUrl)!!,
            (if (running) username else fixedUser)!!,
            (if (running) password else fixedPassword)!!,
            if (running) databaseName else fixedDbName
        )
    }

    init {
        if (fixedEnv) {
            addFixedExposedPort(fixedPort, MYSQL_PORT)
        }
    }

    companion object : KLogging() {
        const val SYS_PROP_URL = "env.db.mysql.url"

        @JvmOverloads
        fun system(
            dockerImageName: String,
            fixedPort: Int = MYSQL_PORT,
            fixedEnv: Boolean = false
        ): ExtSystem<SpecAwareMySqlContainer> =
            ContainerizedSystem(SpecAwareMySqlContainer(dockerImageName, fixedPort, fixedEnv))
    }
}
