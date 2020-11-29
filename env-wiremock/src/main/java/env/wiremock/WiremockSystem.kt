package env.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer
import env.core.Environment.Companion.findAvailableTcpPort
import env.core.Environment.Companion.setProperties
import env.core.GenericExternalSystem
import env.core.PortsExposingStrategy
import env.core.PortsExposingStrategy.SystemPropertyToggle

class WiremockSystem @JvmOverloads constructor(
    portsExposingStrategy: PortsExposingStrategy = SystemPropertyToggle(),
    fixedPort: Int = 8888,
    server: WireMockServer = WireMockServer(
        wireMockConfig().extensions(ResponseTemplateTransformer(true)).port(
            (if (portsExposingStrategy.fixedPorts()) fixedPort else findAvailableTcpPort()).apply {
                mapOf("env.wiremock.port" to this.toString()).setProperties()
            }
        )
    ),
    private val afterStart: WireMockServer.() -> Unit = { }
) : GenericExternalSystem<WireMockServer>(
    system = server,
    start = { it.start(); it.afterStart() },
    stop = { it.stop() },
    running = { it.isRunning }
) {
    @JvmOverloads
    constructor(
        portsExposingStrategy: PortsExposingStrategy = SystemPropertyToggle(),
        afterStart: WireMockServer.() -> Unit
    ) : this(
        portsExposingStrategy = portsExposingStrategy,
        fixedPort = 8888,
        afterStart = afterStart
    )

    override fun describe() = "${system.baseUrl()} registered ${system.listAllStubMappings().mappings.size} mappings."
}
