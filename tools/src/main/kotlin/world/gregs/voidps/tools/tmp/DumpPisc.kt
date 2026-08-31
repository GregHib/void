package world.gregs.voidps.tools.tmp

import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.definition.decoder.ObjectDecoder
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.tools.cache.Xteas
import world.gregs.voidps.tools.map.MapDecoder

object DumpPisc {

    @JvmStatic
    fun main(args: Array<String>) {
        Settings.load()
        val cache = CacheDelegate(Settings["storage.cache.path"])
        val objects = ObjectDecoder(member = true, lowDetail = false).load(cache)
        val decals = mutableMapOf<Int, Int>()
        for (def in objects) {
            if (def.varbit in 2984..3015) {
                decals[def.id] = def.varbit
            }
        }
        val interact = mutableSetOf<Int>()
        for (def in objects) {
            val options = def.options ?: continue
            if (options.any { it == "Inspect" || it == "Search" || it == "Attack" } && def.name != "null" && def.id in 19000..19999) {
                interact.add(def.id)
            }
        }
        val decoder = MapDecoder(Xteas())
        decoder.modified = false
        val maps = decoder.load(cache)
        val regions = mutableSetOf<Int>()
        for (rx in 35..37) {
            for (ry in 54..57) {
                regions.add((rx shl 8) or ry)
            }
        }
        for (definition in maps) {
            if (definition.id !in regions) {
                continue
            }
            val regionX = (definition.id shr 8) shl 6
            val regionY = (definition.id and 0xff) shl 6
            for (obj in definition.objects) {
                if (obj.id in decals || obj.id in interact) {
                    val def = objects.getOrNull(obj.id)
                    println("map obj=${obj.id} '${def?.name}' varbit=${decals[obj.id] ?: -1} x=${regionX + obj.x} y=${regionY + obj.y} level=${obj.level}")
                }
            }
        }
    }
}
