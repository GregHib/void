package world.gregs.voidps.web

import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import world.gregs.voidps.web.api.ApiServices
import world.gregs.voidps.web.api.route.api
import world.gregs.voidps.web.api.route.apiPlugins
import world.gregs.voidps.web.route.proxy
import world.gregs.voidps.web.route.webclient
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.time.Duration.Companion.seconds

/**
 * [services] mounts the REST API described by `web/openapi.yaml` under `/api/v1`. It is optional so
 * the proxy and web client can still be served on their own — [main] runs without a game server to
 * read live state from.
 *
 * [secureCookies] should only be false for plain-HTTP local development.
 */
class WebServer(
    webclientZip: Path,
    port: Int,
    serverAddress: String,
    serverPort: Int,
    services: ApiServices? = null,
    secureCookies: Boolean = true,
) {

    private val embeddedServer = embeddedServer(CIO, port = port) {
        install(WebSockets.Plugin) {
            pingPeriod = 15.seconds
            timeout = 30.seconds
            maxFrameSize = Long.MAX_VALUE
            masking = false
        }
        if (services != null) {
            apiPlugins(services)
        }
        routing {
            get("/") { call.respondRedirect("/play", permanent = true) }
            proxy(serverAddress, serverPort)
            webclient(port, webclientZip)
            if (services != null) {
                api(services, secureCookies = secureCookies)
            }
        }
    }

    fun start() {
        embeddedServer.start(wait = true)
    }

    fun stop() {
        embeddedServer.stop(1000L)
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val path = Paths.get("./data/webclient.zip")
            WebServer(path, 8080, "localhost", 43594).start()
        }
    }
}
