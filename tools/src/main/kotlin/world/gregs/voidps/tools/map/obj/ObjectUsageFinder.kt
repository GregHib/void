package world.gregs.voidps.tools.map.obj

import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.fileProperties
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.definition.data.MapObject
import world.gregs.voidps.cache.definition.decoder.MapDecoder
import world.gregs.voidps.cache.definition.decoder.ObjectDecoder
import world.gregs.voidps.engine.client.cacheDefinitionModule
import world.gregs.voidps.engine.client.cacheModule
import world.gregs.voidps.engine.map.region.Region
import world.gregs.voidps.engine.map.region.XteaLoader
import world.gregs.voidps.engine.map.region.Xteas

object ObjectUsageFinder {

    @JvmStatic
    fun main(args: Array<String>) {
        val koin = startKoin {
            fileProperties("/tool.properties")
            modules(module {
                single(createdAtStart = true) {
                    Xteas(mutableMapOf()).apply {
                        XteaLoader().load(this, getProperty("xteaPath"), getPropertyOrNull("xteaJsonKey"), getPropertyOrNull("xteaJsonValue"))
                    }
                }
            }, cacheModule, cacheDefinitionModule)
        }.koin
        val decoder = ObjectDecoder(koin.get(), member = false, lowDetail = false, configReplace = false)
        val cache: Cache = koin.get()
        val mapDecoder = MapDecoder(cache, koin.get<Xteas>())
        val objects = mutableMapOf<Region, List<MapObject>>()
        for (regionX in 0 until 256) {
            for (regionY in 0 until 256) {
                cache.getFile(5, "m${regionX}_${regionY}") ?: continue
                val region = Region(regionX, regionY)
                val def = mapDecoder.getOrNull(region.id) ?: continue
                objects[region] = def.objects
            }
        }

        for ((region, list) in objects) {
            for (obj in list) {
                val def = decoder.getOrNull(obj.id) ?: continue
                if (obj.type == 0 && def.solid != 1 && !def.blocksSky && !def.ignoreOnRoute && def.options?.any { it != null && it != "Examine" } == true) {
                    println("Found ${obj.id} ${obj.type} - ${region.tile.x + obj.x}, ${region.tile.y + obj.y}, ${obj.plane}")
                }
            }
//            val obj = list.firstOrNull { it.id == objectId } ?: continue
//            println("Found in region ${region.id} ${region.tile.x + obj.x}, ${region.tile.y + obj.y}, ${obj.plane}")
        }
    }
}