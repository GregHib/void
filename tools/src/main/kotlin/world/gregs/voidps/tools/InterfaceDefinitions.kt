package world.gregs.voidps.tools

import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.definition.decoder.InterfaceDecoder

object InterfaceDefinitions {
    @JvmStatic
    fun main(args: Array<String>) {
        val cache: Cache = CacheDelegate(property("cachePath"))
        val decoder = InterfaceDecoder().loadCache(cache)
        for (i in listOf(300)) {//decoder.indices) {
            val def = decoder.getOrNull(i) ?: continue
            for ((id, comp) in def.components ?: continue) {
                    println("$id - $comp")
            }
        }
    }
}