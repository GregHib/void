package world.gregs.voidps.tools

import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.config.decoder.QuestDecoder

object QuestDefinitions {

    @JvmStatic
    fun main(args: Array<String>) {
        val cache: Cache = CacheDelegate(property("cachePath"))
        val decoder = QuestDecoder().loadCache(cache)
        for (i in decoder.indices) {
            val def = decoder.getOrNull(i) ?: continue
            if (def.params != null) {
                println("$i $def")
            }
        }
    }
}