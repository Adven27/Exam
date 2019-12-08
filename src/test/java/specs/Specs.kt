package specs

import com.adven.concordion.extensions.exam.core.ExamExtension
import com.adven.concordion.extensions.exam.db.DbPlugin
import com.adven.concordion.extensions.exam.db.DbTester
import com.adven.concordion.extensions.exam.files.FlPlugin
import com.adven.concordion.extensions.exam.mq.MqPlugin
import com.adven.concordion.extensions.exam.mq.MqTesterAdapter
import com.adven.concordion.extensions.exam.ui.UiPlugin
import com.adven.concordion.extensions.exam.ws.WsPlugin
import com.github.jknack.handlebars.Helper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import org.concordion.api.AfterSuite
import org.concordion.api.BeforeSuite
import org.concordion.api.extension.Extension
import org.concordion.api.extension.Extensions
import org.concordion.api.option.ConcordionOptions
import org.concordion.ext.runtotals.RunTotalsExtension
import org.concordion.ext.timing.TimerExtension
import org.concordion.integration.junit4.ConcordionRunner
import org.concordion.internal.ConcordionBuilder.NAMESPACE_CONCORDION_2007
import org.junit.runner.RunWith
import java.util.*

@SuppressFBWarnings("ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
@RunWith(ConcordionRunner::class)
@Extensions(value = [TimerExtension::class, RunTotalsExtension::class])
@ConcordionOptions(declareNamespaces = ["c", NAMESPACE_CONCORDION_2007, "e", ExamExtension.NS])
open class Specs {

    @SuppressFBWarnings(value = ["URF_UNREAD_FIELD"], justification = "concordion extension declaration")
    @Suppress("unused")
    @Extension
    private val exam = EXAM

    companion object {
        @JvmStatic val dbTester = dbTester()

        const val PORT = 8888
        protected var server: WireMockServer? = WireMockServer(
            wireMockConfig().extensions(ResponseTemplateTransformer(true)).port(PORT)
        )

        private val EXAM = ExamExtension().withPlugins(
            WsPlugin(PORT),
            DbPlugin(dbTester),
            FlPlugin(),
            MqPlugin(
                mapOf("myQueue" to object : MqTesterAdapter() {
                    private val queue = ArrayDeque<Pair<Map<String, Any>, String>>()

                    override fun send(message: String, headers: Map<String, Any>) {
                        queue.add(headers to message)
                    }

                    override fun receiveWithHeaders(): Pair<Map<String, Any>, String> =
                            queue.poll() ?: emptyMap<String, Any>() to ""
                })
            ),
            UiPlugin(screenshotsOnFail = true, screenshotsOnSuccess = false)
        ).withHandlebar { hb ->
            hb.registerHelper("hi", Helper { context: Any?, options ->
                //{{hi '1' 'p1 'p2' o1='a' o2='b'}} => Hello context = 1; params = [p1, p2]; options = {o1=a, o2=b}!
                //{{hi variable1 variable2 o1=variable3}} => Hello context = 1; params = [2]; options = {o1=3}!
                "Hello context = $context; params = ${options.params.map { it.toString() }}; options = ${options.hash}!"
            })
        }.withFocusOnFailed(false)

        @JvmStatic
        @AfterSuite
        @Suppress("unused")
        fun stopServer() {
            if (server != null) {
                server!!.stop()
                server = null
            }
        }

        @JvmStatic
        @BeforeSuite
        @Suppress("unused")
        fun startServer() {
            server?.apply {
                val req = "{{{request.body}}}"
                val cookie = """{"cookies":"{{{request.cookies}}}"}"""
                val method = """{"{{{request.requestLine.method}}}":"{{{request.url}}}"}"""

                stubFor(
                    post(anyUrl()).atPriority(1).withHeader(
                        "Content-type", equalTo("application/soap+xml; charset=UTF-8;")
                    ).willReturn(
                        req status 200
                    )
                )

                stubFor(
                    get(urlPathEqualTo("/ui")).atPriority(1).willReturn(
                        "<html><head></head><body><span>Dummy page</span></body></html>" status 200
                    )
                )

                stubFor(
                    post(urlPathEqualTo("/status/400")).atPriority(1).willReturn(
                        req status 400
                    )
                )
                stubFor(
                    put(urlPathEqualTo("/status/400")).atPriority(1).willReturn(
                        req status 400
                    )
                )
                stubFor(
                    get(urlPathEqualTo("/ignoreJson")).atPriority(1).willReturn(
                            aResponse().withBody("{ \"param1\":\"value1\", \"param2\":\"value2\", " +
                                    "\"arr\": [{\"param3\":\"value3\", \"param4\":\"value4\"}, {\"param3\":\"value3\", \"param4\":\"value4\"}]}")
                    )
                )
                stubFor(
                    get(urlPathEqualTo("/ignoreJsonArray")).atPriority(1).willReturn(
                            aResponse().withBody("[{\"param3\":\"value3\", \"param4\":\"value4\"}, {\"param3\":\"value3\", \"param4\":\"value4\"}]")
                    )
                )
                stubFor(
                    any(urlPathEqualTo("/status/400")).atPriority(2).willReturn(
                        method status 400
                    )
                )

                stubFor(
                    post(anyUrl()).withCookie("cook", matching(".*")).atPriority(3).willReturn(
                        cookie status 200
                    )
                )
                stubFor(
                    put(anyUrl()).withCookie("cook", matching(".*")).atPriority(3).willReturn(
                        cookie status 200
                    )
                )
                stubFor(
                    post(anyUrl()).atPriority(4).willReturn(
                        req status 200
                    )
                )
                stubFor(
                    put(anyUrl()).atPriority(4).willReturn(
                        req status 200
                    )
                )

                stubFor(
                    any(anyUrl()).withCookie("cook", matching(".*")).atPriority(5)
                        .willReturn(
                            aResponse()
                                .withHeader("my_header", "some value")
                                .withBody("""{"{{{request.requestLine.method}}}":"{{{request.url}}}", "cookies":"{{{request.cookies}}}"}""")
                        )
                )
                stubFor(any(anyUrl()).atPriority(6).willReturn(aResponse().withBody(method)))

                start()
            }
        }

        private infix fun String.status(status: Int) = aResponse().withBody(this).withStatus(status)

        private fun dbTester(): DbTester {
            return DbTester(
                "org.h2.Driver", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;INIT=RUNSCRIPT FROM 'classpath:sql/populate.sql'",
                "sa", ""
            )
        }
    }
}