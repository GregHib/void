package world.gregs.voidps.tools.map.render

import org.koin.core.context.startKoin
import org.koin.dsl.module
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.config.decoder.MapSceneDecoder
import world.gregs.voidps.cache.config.decoder.OverlayDecoder
import world.gregs.voidps.cache.config.decoder.UnderlayDecoder
import world.gregs.voidps.cache.config.decoder.WorldMapInfoDecoder
import world.gregs.voidps.cache.definition.decoder.*
import world.gregs.voidps.engine.client.cacheConfigModule
import world.gregs.voidps.engine.client.cacheDefinitionModule
import world.gregs.voidps.engine.client.cacheModule
import world.gregs.voidps.engine.map.region.Region
import world.gregs.voidps.engine.map.region.Xteas
import world.gregs.voidps.engine.map.region.xteaModule
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
            modules(cacheModule, cacheDefinitionModule, cacheConfigModule, xteaModule,
            module {
                single { MapDecoder(get(), get<Xteas>()) }
            })
        }.koin

        val cache: Cache = koin.get()
        val mapDecoder: MapDecoder = koin.get()
        val objectDecoder: ObjectDecoder = koin.get()
        val overlayDefinitions: OverlayDecoder = koin.get()
        val underlayDefinitions: UnderlayDecoder = koin.get()
        val textureDefinitions: TextureDecoder = koin.get()
        val worldMapDecoder: WorldMapDetailsDecoder = koin.get()
        val worldMapInfoDecoder: WorldMapInfoDecoder = koin.get()
        val spriteDecoder: SpriteDecoder = koin.get()
        val mapSceneDecoder: MapSceneDecoder = koin.get()

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