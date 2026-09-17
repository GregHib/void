package world.gregs.voidps.web

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import world.gregs.voidps.web.api.ApiConfig
import world.gregs.voidps.web.api.api
import world.gregs.voidps.web.api.apiPlugins
import world.gregs.voidps.web.route.proxy
import world.gregs.voidps.web.route.webclient
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.time.Duration.Companion.seconds

/**
 * Hosts the webclient and its websocket proxy to the game server, plus the optional account api
 * @param webclientZip webclient files to serve at /play, null to disable
 * @param api account api configuration, null to disable
 */
class WebServer(
    port: Int,
    serverAddress: String,
    serverPort: Int,
    webclientZip: Path?,
    api: ApiConfig?,
) {

    private val embeddedServer = embeddedServer(CIO, port = port) {
        webModule(port, serverAddress, serverPort, webclientZip, api)
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
            WebServer(8080, "localhost", 43594, path, api = null).start()
        }
    }
}

fun Application.webModule(port: Int, serverAddress: String, serverPort: Int, webclientZip: Path?, api: ApiConfig?) {
    install(WebSockets.Plugin) {
        pingPeriod = 15.seconds
        timeout = 30.seconds
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
    if (api != null) {
        apiPlugins(api.token)
    }
    routing {
        proxy(serverAddress, serverPort)
        if (webclientZip != null) {
            get("/") { call.respondRedirect("/play", permanent = true) }
            webclient(port, webclientZip)
        }
        if (api != null) {
            api(api.accounts)
        }
    }
}
