package world.gregs.voidps.web.site

import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.definition.decoder.NPCDecoder
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.configFiles
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

/**
 * Builds static pages out of the reusable components and writes them,
 * plus the design-system stylesheet, into the specified directory.
 */
object Site {

    const val FULL = false
    val version = latestRelease()

    @JvmStatic
    fun main(args: Array<String>) {
        Settings.load("./game/src/main/resources/game.properties")
        val files = configFiles()
        val cache: Cache = CacheDelegate(Settings["storage.cache.path"])

        val npcDefinitions = NPCDecoder(true).load(cache)
        NPCDefinitions.init(npcDefinitions).load(files.getValue(Settings["definitions.npcs"]))
        Areas.load(files.list(Settings["map.areas"]))

        val buildDir = File(Settings["web.server.pages"])
        buildDir.mkdirs()
        val gameData = GameData()
        File(buildDir, "index.html").writeText(Website.homePage())
        // Linked from the header on every build (see [Website.pages]) — [WorldMap] keeps the pieces
        // that need a server bridge (the player pins and the console's players tab) behind [FULL] itself.
        File(buildDir, "world-map.html").writeText(WorldMap.page(MapLabels(cache)))
        if (FULL) {
            File(buildDir, "play.html").writeText(Play.page())
            File(buildDir, "worlds.html").writeText(Website.worldsPage())
            File(buildDir, "exchange.html").writeText(Exchange.page())
            File(buildDir, "hiscores.html").writeText(Hiscores.page(gameData))
            File(buildDir, "log.html").writeText(AdventurersLog.page(gameData))
            val devDir = File(buildDir, "dev")
            devDir.mkdirs()
            File(devDir, "index.html").writeText(Dev.dashboardPage())
            File(devDir, "players.html").writeText(Dev.playersPage())
        }
        copyStaticAssets(buildDir)
        copyMapTiles(buildDir)
        Docs.generate(File("./docs/"), buildDir)
    }

    private fun copyStaticAssets(buildDir: File) {
        val source = File("./web/site/src/main/resources/static")
        if (!source.exists()) {
            return
        }
        source.copyRecursively(target = buildDir, overwrite = true)
    }

    /** Pre-rendered map tiles (see `void-map-tiles`) aren't checked into this repo — copied in
     *  from wherever [Settings] points, the same way [copyStaticAssets] copies `static/`, so
     *  [WorldMap]'s `map-tiles/{level}/{zoom}/{x}/{y}.png` requests resolve when that path is set
     *  up. Silently skipped when it isn't — the map still renders, just without tile imagery. */
    private fun copyMapTiles(buildDir: File) {
        val source = File(Settings["web.map.tiles", "./data/map-tiles/"])
        if (!source.exists()) {
            return
        }
        source.copyRecursively(target = File(buildDir, "map-tiles"), overwrite = true)
    }

    private fun latestRelease(): String {
        try {
            val url = "https://api.github.com/repos/GregHib/void/releases/latest"
            val client = HttpClient.newHttpClient()
            val request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/vnd.github+json")
                .build()

            val response = client.send(request, HttpResponse.BodyHandlers.ofString())

            if (response.statusCode() != 200) {
                throw RuntimeException("Failed to fetch release: ${response.statusCode()} - ${response.body()}")
            }
            return "v${response.body().substringAfter("\"tag_name\":\"").substringBefore("\"")}"
        } catch (e: Exception) {
            e.printStackTrace()
            return "v0.0.0"
        }
    }
}
