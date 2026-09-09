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
import world.gregs.voidps.web.route.proxy
import world.gregs.voidps.web.route.webclient
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.time.Duration.Companion.seconds

class WebServer(
    webclientZip: Path,
    port: Int,
    serverAddress: String,
    serverPort: Int,
) {

    private val embeddedServer = embeddedServer(CIO, port = port) {
        install(WebSockets.Plugin) {
            pingPeriod = 15.seconds
            timeout = 30.seconds
            maxFrameSize = Long.MAX_VALUE
            masking = false
        }
        routing {
            get("/") { call.respondRedirect("/play", permanent = true) }
            proxy(serverAddress, serverPort)
            webclient(port, webclientZip)
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