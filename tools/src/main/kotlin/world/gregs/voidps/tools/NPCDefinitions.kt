package world.gregs.voidps.tools

import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.definition.decoder.NPCDecoder

object NPCDefinitions {
    @JvmStatic
    fun main(args: Array<String>) {
        val cache: Cache = CacheDelegate(property("cachePath"))
        val decoder = NPCDecoder(true).loadCache(cache)
        println(decoder.lastIndex)
        for (i in decoder.indices) {
            val def = decoder.getOrNull(i) ?: continue
            if (def.name.contains("sir prysin", true)) {
                println("$i ${def.name} ${def.walkMask}")
            }
        }
    }
}