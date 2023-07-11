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
import world.gregs.voidps.engine.map.region.Xteas
import world.gregs.voidps.tools.Pipeline
import world.gregs.voidps.tools.map.render.draw.MinimapIconPainter
import world.gregs.voidps.tools.map.render.draw.RegionRenderer
import world.gregs.voidps.tools.map.render.load.MapTileSettings
import world.gregs.voidps.tools.map.render.load.RegionManager
import world.gregs.voidps.type.Region
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
                single { MapDecoder(get<Xteas>()) }
                single(createdAtStart = true) {
                    Xteas().load(getProperty("xteaPath"), getPropertyOrNull("xteaJsonKey") ?: Xteas.DEFAULT_KEY, getPropertyOrNull("xteaJsonValue") ?: Xteas.DEFAULT_VALUE)
                }
            })
        }.koin

        val cache: Cache = koin.get()
        val mapDecoder = MapDecoder(koin.get<Xteas>()).loadCache(cache)
        val objectDecoder = ObjectDecoderFull(member = true, lowDetail = false).loadCache(cache)
        val overlayDefinitions = OverlayDecoder().loadCache(cache)
        val underlayDefinitions = UnderlayDecoder().loadCache(cache)
        val textureDefinitions = TextureDecoder().loadCache(cache)
        val worldMapDecoder = WorldMapDetailsDecoder().loadCache(cache)
        val worldMapInfoDecoder = WorldMapInfoDecoder().loadCache(cache)
        val spriteDecoder = SpriteDecoder().loadCache(cache)
        val mapSceneDecoder = MapSceneDecoder().loadCache(cache)

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