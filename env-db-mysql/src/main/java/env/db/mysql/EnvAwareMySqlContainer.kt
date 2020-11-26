package env.db.mysql

import com.adven.concordion.extensions.exam.db.DbTester
import env.core.Environment.Companion.setProperties
import mu.KLogging
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.utility.DockerImageName

@Suppress("LongParameterList")
class EnvAwareMySqlContainer @JvmOverloads constructor(
    dockerImageName: DockerImageName = DockerImageName.parse("mysql:5.7.22"),
    fixedEnv: Boolean = false,
    fixedPort: Int = MYSQL_PORT,
    private val urlSystemPropertyName: String = "env.db.mysql.url",
    private val usernameSystemPropertyName: String = "env.db.mysql.username",
    private val passwordSystemPropertyName: String = "env.db.mysql.password",
    private val driverSystemPropertyName: String = "env.db.mysql.driver",
    private val afterStart: EnvAwareMySqlContainer.() -> Unit = { }
) : MySQLContainer<Nothing>(dockerImageName) {

    init {
        if (fixedEnv) {
            addFixedExposedPort(fixedPort, MYSQL_PORT)
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

    companion object : KLogging()
}
