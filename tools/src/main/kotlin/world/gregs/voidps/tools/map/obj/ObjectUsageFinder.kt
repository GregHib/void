package world.gregs.voidps.tools.map.obj

import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.definition.data.MapObject
import world.gregs.voidps.cache.definition.data.ObjectDefinitionFull
import world.gregs.voidps.cache.definition.decoder.MapDecoder
import world.gregs.voidps.cache.definition.decoder.ObjectDecoderFull
import world.gregs.voidps.tools.cache.Xteas
import world.gregs.voidps.tools.property
import world.gregs.voidps.tools.propertyOrNull
import world.gregs.voidps.type.Region

object ObjectUsageFinder {

    @JvmStatic
    fun main(args: Array<String>) {
        val cache: Cache = CacheDelegate(property("cachePath"))
        val xteas: Xteas = Xteas().load(property("xteaPath"), propertyOrNull("xteaJsonKey") ?: Xteas.DEFAULT_KEY, propertyOrNull("xteaJsonValue") ?: Xteas.DEFAULT_VALUE)
        val decoder = ObjectDecoderFull(members = false, lowDetail = false).loadCache(cache)
        val maps = MapDecoder(xteas).loadCache(cache)
        for (map in maps) {
            val region = Region(map.id)
            for (obj in map.objects) {
                val def = decoder.getOrNull(obj.id) ?: continue
                if (matches(obj, def)) {
                    println("Found ${obj.id} - ${region.tile.x + obj.x}, ${region.tile.y + obj.y}, ${obj.level} ${def.varbit}")
                }
            }
        }
    }

    private fun matches(obj: MapObject, def: ObjectDefinitionFull) = obj.id == 24368 || obj.id == 2397
}