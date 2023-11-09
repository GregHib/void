package world.gregs.voidps.tools

import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.definition.decoder.ItemDecoderFull

object ItemDefinitions {
    @JvmStatic
    fun main(args: Array<String>) {
        val cache: Cache = CacheDelegate(property("cachePath"))
        val decoder = ItemDecoderFull().loadCache(cache)
        println(decoder.lastIndex)
        for (i in decoder.indices) {
            val def = decoder.getOrNull(i) ?: continue
            if (def.dummyItem != 0) {
                println("$i ${def.name} ${def.dummyItem}")
            }
        }
    }
}