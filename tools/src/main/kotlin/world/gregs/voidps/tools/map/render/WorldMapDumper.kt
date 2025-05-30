package world.gregs.voidps.tools.map.render

import org.koin.core.context.startKoin
import org.koin.dsl.module
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.MemoryCache
import world.gregs.voidps.cache.config.decoder.MapSceneDecoder
import world.gregs.voidps.cache.config.decoder.OverlayDecoder
import world.gregs.voidps.cache.config.decoder.UnderlayDecoder
import world.gregs.voidps.cache.config.decoder.WorldMapInfoDecoder
import world.gregs.voidps.cache.definition.decoder.*
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.tools.Pipeline
import world.gregs.voidps.tools.cache.Xteas
import world.gregs.voidps.tools.map.MapDecoder
import world.gregs.voidps.tools.map.render.draw.MinimapIconPainter
import world.gregs.voidps.tools.map.render.draw.RegionRenderer
import world.gregs.voidps.tools.map.render.load.MapTileSettings
import world.gregs.voidps.tools.map.render.load.RegionManager
import world.gregs.voidps.type.Region
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Renders and saves all regions to individual pngs with various levels of detail
 */
object WorldMapDumper {

    var minimapIcons = true

    @JvmStatic
    fun main(args: Array<String>) {
        Settings.load("game.properties")
        val koin = startKoin {
            modules(
            module {
                single { MemoryCache(Settings["storage.cache.path"]) as Cache }
                single { MapDecoder(get<Xteas>()) }
                single(createdAtStart = true) {
                    Xteas()//.load(Settings["storage.xteas"], Settings["xteaJsonKey", Xteas.DEFAULT_KEY], Settings["xteaJsonValue", Xteas.DEFAULT_VALUE])
                }
            })
        }.koin

        val cache: Cache = koin.get()
        val mapDefinitions = MapDecoder(koin.get<Xteas>()).load(cache).associateBy { it.id }
        val objectDecoder = ObjectDecoderFull(members = true, lowDetail = false).load(cache)
        val overlayDefinitions = OverlayDecoder().load(cache)
        val underlayDefinitions = UnderlayDecoder().load(cache)
        val textureDefinitions = MaterialDecoder().load(cache)
        val worldMapDecoder = WorldMapDetailsDecoder().load(cache)
        val worldMapInfoDecoder = WorldMapInfoDecoder().load(cache)
        val spriteDecoder = SpriteDecoder().load(cache)
        val mapSceneDecoder = MapSceneDecoder().load(cache)

        File("./images/").mkdir()
        for (i in 0 until 4) {
            File("./images/$i/").mkdir()
        }

        val loader = MinimapIconPainter(objectDecoder, worldMapDecoder, worldMapInfoDecoder, spriteDecoder)
        loader.startup(cache)
        val manager = RegionManager(mapDefinitions, 3)
        val settings = MapTileSettings(4, underlayDefinitions, overlayDefinitions, textureDefinitions, manager = manager)

        val pipeline = Pipeline<Region>()
        pipeline.add(RegionRenderer(manager, objectDecoder, spriteDecoder, mapSceneDecoder, loader, settings))

        val regions = mutableListOf<Region>()
        for (regionX in 0 until 256) {
            for (regionY in 0 until 256) {
                cache.data(5, "m${regionX}_${regionY}") ?: continue
                regions.add(Region(regionX, regionY))
            }
        }
        val start = System.currentTimeMillis()

        regions.forEach {
            pipeline.process(it)
        }
        println("${regions.size} regions loaded in ${TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - start)}s")
    }
}