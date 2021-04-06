package specs

import com.github.jknack.handlebars.Helper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.any
import com.github.tomakehurst.wiremock.client.WireMock.anyUrl
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.matching
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.put
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer
import io.github.adven27.concordion.extensions.exam.core.AbstractSpecs
import io.github.adven27.concordion.extensions.exam.core.ExamExtension
import io.github.adven27.concordion.extensions.exam.db.DbPlugin
import io.github.adven27.concordion.extensions.exam.db.DbTester
import io.github.adven27.concordion.extensions.exam.db.DbUnitConfig
import io.github.adven27.concordion.extensions.exam.db.commands.IgnoreMillisComparer
import io.github.adven27.concordion.extensions.exam.files.FlPlugin
import io.github.adven27.concordion.extensions.exam.mq.MqPlugin
import io.github.adven27.concordion.extensions.exam.mq.MqTester
import io.github.adven27.concordion.extensions.exam.ui.UiPlugin
import io.github.adven27.concordion.extensions.exam.ws.WsPlugin
import org.concordion.api.extension.Extensions
import org.concordion.ext.runtotals.RunTotalsExtension
import org.concordion.ext.timing.TimerExtension
import java.util.ArrayDeque

@Extensions(value = [TimerExtension::class, RunTotalsExtension::class])
open class Specs : AbstractSpecs() {

    override fun init() = ExamExtension(
        WsPlugin(PORT),
        DbPlugin(dbTester),
        FlPlugin(),
        MqPlugin(
            mapOf(
                "myQueue" to object : MqTester.NOOP() {
                    private val queue = ArrayDeque<MqTester.Message>()

                    override fun send(message: String, headers: Map<String, String>) {
                        queue += MqTester.Message(message, headers)
                    }

                    override fun receive() = queue.map { queue.poll() }

                    override fun purge() = queue.clear()
                }
            )
        ),
        UiPlugin(screenshotsOnFail = true, screenshotsOnSuccess = false)
    ).withHandlebar { hb ->
        hb.registerHelper(
            "hi",
            Helper { context: Any?, options ->
                // {{hi '1' 'p1 'p2' o1='a' o2='b'}} => Hello context = 1; params = [p1, p2]; options = {o1=a, o2=b}!
                // {{hi variable1 variable2 o1=variable3}} => Hello context = 1; params = [2]; options = {o1=3}!
                "Hello context = $context; params = ${options.params.map { it.toString() }}; options = ${options.hash}!"
            }
        )
    }.withFocusOnFailed(false)

    override fun startSut() {
        server.apply {
            configure()
            start()
        }
    }

    override fun stopSut() = server.stop()

    companion object {
        @JvmStatic
        val dbTester = dbTester()

        const val PORT = 8888
        protected var server: WireMockServer = WireMockServer(
            wireMockConfig().extensions(ResponseTemplateTransformer(true)).port(PORT)
        )

        private fun dbTester() = DbTester(
            "org.h2.Driver", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;INIT=RUNSCRIPT FROM 'classpath:sql/populate.sql'",
            "sa", "",
            dbUnitConfig = DbUnitConfig(columnValueComparers = mapOf("DATETIME_TYPE" to IgnoreMillisComparer()))
        )

        @Suppress("LongMethod")
        private fun WireMockServer.configure() {
            val req = "{{{request.body}}}"
            val cookie = """{"cookies":"{{{request.cookies}}}"}"""
            val method = """{"{{{request.requestLine.method}}}":"{{{request.url}}}"}"""
            val mirror = """
                    |{
                    |   "{{request.requestLine.method}}":"{{request.url}}", 
                    |   "request.custom.headers":["{{request.headers.h1}}", "{{request.headers.h2}}"]
                    |}"""
                .trimMargin()

            stubFor(any(urlPathEqualTo("/mirror")).atPriority(1).willReturn(mirror status 200))

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
                get(urlPathEqualTo("/ignoreJson")).atPriority(1).willReturn(
                    aResponse().withBody(
                        """{ 
                            |"param1":"value1", 
                            |"param2":"value2", 
                            |"arr": [{"param3":"value3", "param4":"value4"}, {"param3":"value3", "param4":"value4"}]
                            |}""".trimMargin()
                    )
                )
            )
            stubFor(
                get(urlPathEqualTo("/ignoreJsonArray")).atPriority(1).willReturn(
                    aResponse().withBody(
                        """[
                            |{"param3":"value3", "param4":"value4"}, 
                            |{"param3":"value3", "param4":"value4"}
                            |]""".trimMargin()
                    )
                )
            )
            stubFor(
                get(urlPathEqualTo("/text")).atPriority(1).willReturn(
                    aResponse().withBody(
                        """text to
                            |compare""".trimMargin()
                    )
                )
            )
            stubFor(
                any(urlPathEqualTo("/method/withParams")).atPriority(1).willReturn(
                    aResponse().withBody(""" { "request" : $method, "body": $req }""")
                )
            )
            stubFor(
                post(urlPathEqualTo("/multipart/url")).atPriority(1).willReturn(
                    aResponse().withHeader("Content-Type", "application/json")
                        .withBody(
                            "{ {{regexExtract request.body 'name=\"(.*?)\"' 'nameP'}} \"part1\": \"{{nameP.0}}\"," +
                                " {{regexExtract request.body 'filename=\"(.*?)\"' 'fileP' }} \"fileName\": \"{{fileP.0}}\"," +
                                " {{regexExtract request.body 'name=\"(jsonPart)\"' 'jsonP' }} \"part1\": \"{{jsonP.0}}\"," +
                                " {{regexExtract request.body '\"(.*?)\": \"(.*?)\"' 'jsonC' }} \"json\": {\"{{jsonC.0}}\" : \"{{jsonC.1}}\" }" +
                                "}"
                        )
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
        }

        private infix fun String.status(status: Int) = aResponse().withBody(this).withStatus(status)
    }
}
