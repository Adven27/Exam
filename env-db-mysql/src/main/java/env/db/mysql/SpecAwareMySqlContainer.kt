package env.db.mysql

import com.adven.concordion.extensions.exam.db.DbTester
import mu.KLogging
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.utility.DockerImageName

class SpecAwareMySqlContainer @JvmOverloads constructor(
    dockerImageName: DockerImageName = DockerImageName.parse("mysql:5.7.22"),
    fixedEnv: Boolean = false,
    fixedPort: Int = MYSQL_PORT,
    private val afterStart: SpecAwareMySqlContainer.() -> Unit = { }
) : MySQLContainer<Nothing>(dockerImageName) {

    init {
        if (fixedEnv) {
            addFixedExposedPort(fixedPort, MYSQL_PORT)
        }
    }

    override fun start() {
        super.start()
        System.setProperty(SYS_PROP_URL, jdbcUrl)
            .also { logger.info("System property set: $SYS_PROP_URL = ${System.getProperty(SYS_PROP_URL)} ") }
        apply(afterStart)
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

    companion object : KLogging() {
        const val SYS_PROP_URL = "env.db.mysql.url"
    }
}
