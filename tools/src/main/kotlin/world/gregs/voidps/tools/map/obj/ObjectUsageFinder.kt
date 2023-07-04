package world.gregs.voidps.tools.map.obj

import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.definition.data.MapObject
import world.gregs.voidps.cache.definition.decoder.MapDecoder
import world.gregs.voidps.cache.definition.decoder.ObjectDecoder
import world.gregs.voidps.engine.map.region.Region
import world.gregs.voidps.engine.map.region.XteaLoader
import world.gregs.voidps.engine.map.region.Xteas
import world.gregs.voidps.tools.property
import world.gregs.voidps.tools.propertyOrNull

object ObjectUsageFinder {

    @JvmStatic
    fun main(args: Array<String>) {
        val cache: Cache = CacheDelegate(property("cachePath"))
        val xteas: Xteas = Xteas(mutableMapOf()).apply {
            XteaLoader().load(this, property("xteaPath"), propertyOrNull("xteaJsonKey"), propertyOrNull("xteaJsonValue"))
        }
        val decoder = ObjectDecoder(member = false, lowDetail = false).loadCache(cache)
        val mapDecoder = MapDecoder(xteas).loadCache(cache)
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
                if (obj.shape == 0 && def.solid != 1 && !def.blocksSky && !def.ignoreOnRoute && def.options?.any { it != null && it != "Examine" } == true) {
                    println("Found ${obj.id} ${obj.shape} - ${region.tile.x + obj.x}, ${region.tile.y + obj.y}, ${obj.level}")
                }
            }
//            val obj = list.firstOrNull { it.id == objectId } ?: continue
//            println("Found in region ${region.id} ${region.tile.x + obj.x}, ${region.tile.y + obj.y}, ${obj.level}")
        }
    }
}