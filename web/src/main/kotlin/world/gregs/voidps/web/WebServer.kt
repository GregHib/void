package world.gregs.voidps.web

import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.http.content.staticFiles
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import world.gregs.voidps.engine.data.Storage
import world.gregs.voidps.engine.data.file.FileStorage
import world.gregs.voidps.web.api.route.api
import world.gregs.voidps.web.api.route.apiPlugins
import world.gregs.voidps.web.route.proxy
import world.gregs.voidps.web.route.webclient
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.time.Duration.Companion.seconds

class WebServer(
    webclientZip: Path?,
    port: Int,
    serverAddress: String,
    serverPort: Int,
    storage: Storage
) {
    private val embeddedServer = embeddedServer(CIO, port = port) {
        install(WebSockets.Plugin) {
            pingPeriod = 15.seconds
            timeout = 30.seconds
            maxFrameSize = Long.MAX_VALUE
            masking = false
        }
        apiPlugins()
        routing {
            staticFiles("/", File("./web/site/build/"))
            if (webclientZip != null) {
                proxy(serverAddress, serverPort)
                webclient(port, webclientZip)
            }
            api(storage)
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
            val storage = FileStorage(File("./data/saves"))
            WebServer(path, 8080, "localhost", 43594, storage).start()
        }
    }
}
