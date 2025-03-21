package world.gregs.voidps.tools.map.obj

import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.definition.data.MapObject
import world.gregs.voidps.cache.definition.data.ObjectDefinitionFull
import world.gregs.voidps.cache.definition.decoder.ObjectDecoderFull
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.tools.cache.Xteas
import world.gregs.voidps.tools.map.MapDecoder
import world.gregs.voidps.type.Region

object ObjectUsageFinder {

    @JvmStatic
    fun main(args: Array<String>) {
        Settings.load()
        val cache: Cache = CacheDelegate(Settings["storage.cache.path"])
        val xteas = Xteas()
        val decoder = ObjectDecoderFull(members = false, lowDetail = false).load(cache)
        val maps = MapDecoder(xteas).load(cache)
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

    private fun matches(obj: MapObject, def: ObjectDefinitionFull) = obj.id == 30726
}