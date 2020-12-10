package rs.dusk.tools.map.render

import org.koin.core.context.startKoin
import rs.dusk.cache.Cache
import rs.dusk.cache.config.decoder.MapSceneDecoder
import rs.dusk.cache.config.decoder.OverlayDecoder
import rs.dusk.cache.config.decoder.UnderlayDecoder
import rs.dusk.cache.config.decoder.WorldMapInfoDecoder
import rs.dusk.cache.definition.decoder.ObjectDecoder
import rs.dusk.cache.definition.decoder.SpriteDecoder
import rs.dusk.cache.definition.decoder.TextureDecoder
import rs.dusk.cache.definition.decoder.WorldMapDecoder
import rs.dusk.engine.client.cacheConfigModule
import rs.dusk.engine.client.cacheDefinitionModule
import rs.dusk.engine.client.cacheModule
import rs.dusk.engine.map.region.Region
import rs.dusk.engine.map.region.obj.GameObjectMapDecoder
import rs.dusk.engine.map.region.obj.Xteas
import rs.dusk.engine.map.region.obj.objectMapDecoderModule
import rs.dusk.engine.map.region.obj.xteaModule
import rs.dusk.engine.map.region.tile.TileDecoder
import rs.dusk.tools.Pipeline
import rs.dusk.tools.map.render.draw.MinimapIconPainter
import rs.dusk.tools.map.render.draw.RegionRenderer
import rs.dusk.tools.map.render.load.MapTileSettings
import rs.dusk.tools.map.render.load.RegionManager
import java.io.File

/**
 * Renders and saves all regions to individual pngs
 */
object WorldMapDumper {

    @JvmStatic
    fun main(args: Array<String>) {
        val koin = startKoin {
            fileProperties("/tool.properties")
            modules(cacheModule, cacheDefinitionModule, cacheConfigModule, xteaModule, objectMapDecoderModule)
        }.koin

        val cache: Cache = koin.get()
        val xteas: Xteas = koin.get()
        val tileDecoder = TileDecoder()
        val objectDecoder: ObjectDecoder = koin.get()
        val mapObjDecoder: GameObjectMapDecoder = koin.get()
        val overlayDefinitions: OverlayDecoder = koin.get()
        val underlayDefinitions: UnderlayDecoder = koin.get()
        val textureDefinitions: TextureDecoder = koin.get()
        val worldMapDecoder: WorldMapDecoder = koin.get()
        val worldMapInfoDecoder: WorldMapInfoDecoder = koin.get()
        val spriteDecoder: SpriteDecoder = koin.get()
        val mapSceneDecoder: MapSceneDecoder = koin.get()

        File("./images/").mkdir()

        val loader = MinimapIconPainter(objectDecoder, worldMapDecoder, worldMapInfoDecoder, spriteDecoder)
        loader.startup(cache)
        val manager = RegionManager(cache, tileDecoder, xteas, mapObjDecoder, 3)
        val settings = MapTileSettings(2, underlayDefinitions, overlayDefinitions, textureDefinitions, manager = manager)

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