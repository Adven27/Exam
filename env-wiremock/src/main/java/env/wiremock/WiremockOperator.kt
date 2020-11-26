package env.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer
import env.core.Environment.Companion.findAvailableTcpPort
import env.core.Environment.Companion.setProperties
import env.core.GenericOperator

class WiremockOperator @JvmOverloads constructor(
    fixedEnv: Boolean = false,
    fixedPort: Int = 8888,
    server: WireMockServer = WireMockServer(
        wireMockConfig().extensions(ResponseTemplateTransformer(true)).port(
            (if (fixedEnv) fixedPort else findAvailableTcpPort()).apply {
                mapOf("env.wiremock.port" to this.toString()).setProperties()
            }
        )
    ),
    private val afterStart: WireMockServer.() -> Unit = { }
) : GenericOperator<WireMockServer>(
    system = server,
    start = { it.start(); it.afterStart() },
    stop = { it.stop() },
    running = { it.isRunning }
)
