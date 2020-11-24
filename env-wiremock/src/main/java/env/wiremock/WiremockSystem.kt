package env.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer
import env.core.Environment.Companion.findAvailableTcpPort
import env.core.GenericExtSystem

class WiremockSystem @JvmOverloads constructor(
    fixedEnv: Boolean = false,
    fixedPort: Int = 8888,
    server: WireMockServer = WireMockServer(
        wireMockConfig().extensions(ResponseTemplateTransformer(true)).port(
            if (fixedEnv) fixedPort else findAvailableTcpPort().apply {
                System.setProperty("env.wiremock.port", this.toString())
            }
        )
    )
) : GenericExtSystem<WireMockServer>(
    server,
    start = { it.start() },
    stop = { it.stop() },
    running = { it.isRunning }
)
