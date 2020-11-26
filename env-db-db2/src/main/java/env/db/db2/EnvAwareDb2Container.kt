package env.db.db2

import com.adven.concordion.extensions.exam.db.DbTester
import env.core.Environment.Companion.setProperties
import mu.KLogging
import org.testcontainers.containers.Db2Container
import org.testcontainers.utility.DockerImageName

@Suppress("unused", "LongParameterList")
class EnvAwareDb2Container @JvmOverloads constructor(
    dockerImageName: DockerImageName,
    fixedEnv: Boolean = false,
    fixedPort: Int = DB2_PORT,
    private val urlSystemPropertyName: String = "env.db.db2.url",
    private val usernameSystemPropertyName: String = "env.db.db2.username",
    private val passwordSystemPropertyName: String = "env.db.db2.password",
    private val driverSystemPropertyName: String = "env.db.db2.driver",
    private val afterStart: EnvAwareDb2Container.() -> Unit = { }
) : Db2Container(dockerImageName) {

    init {
        acceptLicense()
        if (fixedEnv) {
            addFixedExposedPort(fixedPort, DB2_PORT)
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
    fun dbTester(port: Int = DB2_PORT): DbTester =
        dbTester("jdbc:db2://localhost:$port/test", "db2inst1", "foobar1234")

    fun dbTester(fixedUrl: String?, fixedUser: String?, fixedPassword: String?): DbTester {
        val running = isRunning
        return DbTester(
            "com.ibm.db2.jcc.DB2Driver",
            (if (running) jdbcUrl else fixedUrl)!!,
            (if (running) username else fixedUser)!!,
            (if (running) password else fixedPassword)!!,
            null
        )
    }

    companion object : KLogging()
}
