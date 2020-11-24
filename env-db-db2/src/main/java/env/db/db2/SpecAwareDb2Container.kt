package env.db.db2

import com.adven.concordion.extensions.exam.db.DbTester
import mu.KLogging
import org.testcontainers.containers.Db2Container
import org.testcontainers.utility.DockerImageName

class SpecAwareDb2Container @JvmOverloads constructor(
    dockerImageName: DockerImageName,
    fixedEnv: Boolean = false,
    fixedPort: Int = DB2_PORT,
    private val afterStart: SpecAwareDb2Container.() -> Unit = { }
) : Db2Container(dockerImageName) {

    init {
        acceptLicense()
        if (fixedEnv) {
            addFixedExposedPort(fixedPort, DB2_PORT)
        }
    }

    override fun start() {
        super.start()
        System.setProperty(SYS_PROP_URL, jdbcUrl)
            .also { logger.info("System property set: $SYS_PROP_URL = ${System.getProperty(SYS_PROP_URL)} ") }
        apply(afterStart)
    }

    @JvmOverloads
    fun dbTester(port: Int = DB2_PORT): DbTester =
        dbTester("jdbc:db2://localhost:$port/test", "db2inst1", "foobar1234", "test")

    fun dbTester(fixedUrl: String?, fixedUser: String?, fixedPassword: String?, fixedDbName: String?): DbTester {
        val running = isRunning
        return DbTester(
            "com.ibm.db2.jcc.DB2Driver",
            (if (running) jdbcUrl else fixedUrl)!!,
            (if (running) username else fixedUser)!!,
            (if (running) password else fixedPassword)!!,
            if (running) databaseName else fixedDbName
        )
    }

    companion object : KLogging() {
        const val SYS_PROP_URL = "env.db.db2.url"
    }
}
