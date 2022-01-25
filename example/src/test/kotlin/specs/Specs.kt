package specs

import app.start
import app.stop
import com.github.jknack.handlebars.Helper
import io.github.adven27.concordion.extensions.exam.core.AbstractSpecs
import io.github.adven27.concordion.extensions.exam.core.ExamExtension
import io.github.adven27.concordion.extensions.exam.core.JsonContentTypeConfig
import io.github.adven27.concordion.extensions.exam.core.JsonVerifier
import io.github.adven27.concordion.extensions.exam.core.TextContentTypeConfig
import io.github.adven27.concordion.extensions.exam.db.DbPlugin
import io.github.adven27.concordion.extensions.exam.db.DbTester
import io.github.adven27.concordion.extensions.exam.db.DbUnitConfig
import io.github.adven27.concordion.extensions.exam.db.commands.IgnoreMillisComparer
import io.github.adven27.concordion.extensions.exam.files.FlPlugin
import io.github.adven27.concordion.extensions.exam.mq.MqPlugin
import io.github.adven27.concordion.extensions.exam.mq.MqTester
import io.github.adven27.concordion.extensions.exam.ui.UiPlugin
import io.github.adven27.concordion.extensions.exam.ws.WsPlugin
import net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_FIELDS
import java.util.ArrayDeque

open class Specs : AbstractSpecs() {

    override fun init() = ExamExtension(
        WsPlugin(PORT.apply { System.setProperty("server.port", this.toString()) }),
        DbPlugin(dbTester),
        MqPlugin(
            mapOf(
                "myQueue" to object : MqTester.NOOP() {
                    private val queue = ArrayDeque<MqTester.Message>()

                    override fun send(message: MqTester.Message, params: Map<String, String>) {
                        queue += message
                    }

                    override fun receive() = queue.map { queue.poll() }
                    override fun purge() = queue.clear()
                }
            )
        ),
        FlPlugin(),
        UiPlugin(baseUrl = "http://localhost:$PORT")
    ).withHandlebar { hb ->
        hb.registerHelper(
            "hi",
            Helper { context: Any?, options ->
                /*
                    {{hi '1' 'p1 'p2' o1='a' o2='b'}} => Hello context = 1; params = [p1, p2]; options = {o1=a, o2=b}!
                    {{hi variable1 variable2 o1=variable3}} => Hello context = 1; params = [2]; options = {o1=3}!
                 */
                "Hello context = $context; params = ${options.params.map { it.toString() }}; options = ${options.hash}!"
            }
        )
    }.withContentTypeConfigs(
        mapOf(
            "customFormat" to TextContentTypeConfig(),
            "jsonIgnoreExtraFields" to JsonContentTypeConfig(
                verifier = JsonVerifier { it.withOptions(IGNORING_EXTRA_FIELDS) }
            ),
            "jsonIgnorePaths" to JsonContentTypeConfig(
                verifier = JsonVerifier { it.whenIgnoringPaths("param2", "arr[*].param4") }
            ),
        )
    )

    override fun startSut() {
        start()
    }

    override fun stopSut() {
        stop()
    }

    companion object {
        const val PORT = 8888

        @JvmStatic
        val dbTester = dbTester()

        private fun dbTester() = DbTester(
            "org.h2.Driver", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;INIT=RUNSCRIPT FROM 'classpath:sql/populate.sql'",
            "sa", "",
            dbUnitConfig = DbUnitConfig(columnValueComparers = mapOf("DATETIME_TYPE" to IgnoreMillisComparer()))
        )
    }
}
