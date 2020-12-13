package rs.dusk.tools.map.process

import org.koin.core.context.startKoin
import rs.dusk.cache.Cache
import rs.dusk.cache.config.decoder.WorldMapInfoDecoder
import rs.dusk.cache.definition.decoder.ClientScriptDecoder
import rs.dusk.cache.definition.decoder.ObjectDecoder
import rs.dusk.cache.definition.decoder.SpriteDecoder
import rs.dusk.engine.client.cacheConfigModule
import rs.dusk.engine.client.cacheDefinitionModule
import rs.dusk.engine.client.cacheModule
import rs.dusk.engine.map.region.Region
import rs.dusk.engine.map.region.obj.*
import rs.dusk.engine.map.region.tile.TileDecoder
import rs.dusk.tools.Pipeline

object WorldMapDataDumper {

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
        val mapInfoDecoder: WorldMapInfoDecoder = koin.get()
        val scriptDecoder: ClientScriptDecoder = ClientScriptDecoder(cache)
        val spriteDecoder: SpriteDecoder = koin.get()
        val set = mutableSetOf<Int>()
        val pipeline = Pipeline<Region>()
        pipeline.add(ObjectProcessor(tileDecoder, mapObjDecoder, objectDecoder, xteas, cache, mapInfoDecoder, scriptDecoder))
        val regions = mutableListOf<Region>()
        for (regionX in 0 until 256) {
            for (regionY in 0 until 256) {
                cache.getFile(5, "m${regionX}_${regionY}") ?: continue
                regions.add(Region(regionX, regionY))
            }
        }
        val start = System.currentTimeMillis()
        regions.forEach {
            if(it.id != 11829) {
                return@forEach
            }
            pipeline.process(it)
        }
        println("${regions.size} regions loaded in ${System.currentTimeMillis() - start}ms")
    }


}