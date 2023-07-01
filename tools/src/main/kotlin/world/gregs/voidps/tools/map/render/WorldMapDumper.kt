package world.gregs.voidps.tools.map.render

import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.fileProperties
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.config.decoder.MapSceneDecoder
import world.gregs.voidps.cache.config.decoder.OverlayDecoder
import world.gregs.voidps.cache.config.decoder.UnderlayDecoder
import world.gregs.voidps.cache.config.decoder.WorldMapInfoDecoder
import world.gregs.voidps.cache.definition.decoder.*
import world.gregs.voidps.engine.map.region.Region
import world.gregs.voidps.engine.map.region.XteaLoader
import world.gregs.voidps.engine.map.region.Xteas
import world.gregs.voidps.tools.Pipeline
import world.gregs.voidps.tools.map.render.draw.MinimapIconPainter
import world.gregs.voidps.tools.map.render.draw.RegionRenderer
import world.gregs.voidps.tools.map.render.load.MapTileSettings
import world.gregs.voidps.tools.map.render.load.RegionManager
import java.io.File

/**
 * Renders and saves all regions to individual pngs
 */
object WorldMapDumper {

    var minimapIcons = false

    @JvmStatic
    fun main(args: Array<String>) {
        val koin = startKoin {
            fileProperties("/tool.properties")
            modules(
            module {
                single { CacheDelegate(getProperty("cachePath")) as Cache }
                single { MapDecoder(get(), get<Xteas>()) }
                single(createdAtStart = true) {
                    Xteas(mutableMapOf()).apply {
                        XteaLoader().load(this, getProperty("xteaPath"), getPropertyOrNull("xteaJsonKey"), getPropertyOrNull("xteaJsonValue"))
                    }
                }
            })
        }.koin

        val cache: Cache = koin.get()
        val mapDecoder = MapDecoder(cache, koin.get<Xteas>())
        val objectDecoder = ObjectDecoder(cache, member = true, lowDetail = false)
        val overlayDefinitions = OverlayDecoder(cache)
        val underlayDefinitions = UnderlayDecoder(cache)
        val textureDefinitions = TextureDecoder(cache)
        val worldMapDecoder = WorldMapDetailsDecoder(cache)
        val worldMapInfoDecoder = WorldMapInfoDecoder(cache)
        val spriteDecoder = SpriteDecoder(cache)
        val mapSceneDecoder = MapSceneDecoder(cache)

        File("./images/").mkdir()
        for (i in 0 until 4) {
            File("./images/$i/").mkdir()
        }

        val loader = MinimapIconPainter(objectDecoder, worldMapDecoder, worldMapInfoDecoder, spriteDecoder)
        loader.startup(cache)
        val manager = RegionManager(mapDecoder, 3)
        val settings = MapTileSettings(4, underlayDefinitions, overlayDefinitions, textureDefinitions, manager = manager)

        val pipeline = Pipeline<Region>()
        pipeline.add(RegionRenderer(manager, objectDecoder, spriteDecoder, mapSceneDecoder, loader, settings))

        val regions = mutableListOf<Region>()
        for (regionX in 0 until 256) {
            for (regionY in 0 until 256) {
                cache.getFile(5, "m${regionX}_${regionY}") ?: continue
                regions.add(Region(regionX, regionY))
            }
        }
        val start = System.currentTimeMillis()

        regions.forEach {
            pipeline.process(it)
        }
        println("${regions.size} regions loaded in ${System.currentTimeMillis() - start}ms")
    }
}