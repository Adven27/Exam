package env.db.db2

import com.adven.concordion.extensions.exam.db.DbTester
import env.core.ContainerizedSystem
import env.core.ExtSystem
import mu.KLogging
import org.testcontainers.containers.Db2Container

class SpecAwareDb2Container @JvmOverloads constructor(
    dockerImageName: String,
    fixedPort: Int = DB2_PORT,
    fixedEnv: Boolean = false
) : Db2Container(dockerImageName) {
    override fun start() {
        super.start()
        System.setProperty(SYS_PROP_URL, jdbcUrl)
            .also { logger.info("System property set: $SYS_PROP_URL = ${System.getProperty(SYS_PROP_URL)} ") }
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

    init {
        if (fixedEnv) {
            addFixedExposedPort(fixedPort, DB2_PORT)
        }
    }

    companion object : KLogging() {
        const val SYS_PROP_URL = "env.db.db2.url"

        @JvmOverloads
        fun system(
            dockerImageName: String,
            fixedPort: Int = DB2_PORT,
            fixedEnv: Boolean = false
        ): ExtSystem<SpecAwareDb2Container> =
            ContainerizedSystem(SpecAwareDb2Container(dockerImageName, fixedPort, fixedEnv))
    }
}
