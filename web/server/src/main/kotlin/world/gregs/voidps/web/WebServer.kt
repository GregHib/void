package world.gregs.voidps.web

import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.http.content.staticFiles
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.definition.decoder.ItemDecoder
import world.gregs.voidps.cache.definition.decoder.NPCDecoder
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.Storage
import world.gregs.voidps.engine.data.configFiles
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.data.definition.QuestDefinitions
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
    storage: Storage,
    questDefinitions: QuestDefinitions
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
            val file = File(Settings["web.server.pages"])
            if (!file.exists() || file.isDirectory && file.list()?.size == 0) {
                get("/") {
                    log.warn("No website found at: $file")
                    call.respondText("No website found, please run `gradle web:site:run`")
                }
            } else {
                staticFiles("/", file)
            }
            if (webclientZip != null) {
                proxy(serverAddress, serverPort)
                webclient(port, webclientZip)
            }
            api(storage, questDefinitions)
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
            Settings.load("./game/src/main/resources/game.properties")
            val files = configFiles()
            val cache: Cache = CacheDelegate(Settings["storage.cache.path"])

            val npcDefinitions = NPCDecoder(true).load(cache)
            NPCDefinitions.init(npcDefinitions).load(files.getValue(Settings["definitions.npcs"]))

            val questDefinitions = QuestDefinitions().load(files.find(Settings["definitions.quests"]))
            ItemDefinitions.init(ItemDecoder().load(cache)).load(files.list(Settings["definitions.items"]))
            val path = Paths.get(Settings["web.client.zip"])
            val storage = FileStorage(File(Settings["storage.players.path"]))
            WebServer(path, 8080, "localhost", 43594, storage, questDefinitions).start()
        }
    }
}
