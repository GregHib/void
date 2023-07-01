package world.gregs.voidps.tools

import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.config.decoder.IdentityKitDecoder

object IdentityKitDefinitions {
    @JvmStatic
    fun main(args: Array<String>) {
        val cache: Cache = CacheDelegate(property("cachePath"))
        val decoder = IdentityKitDecoder(cache)
        println(decoder.last)
        for (i in decoder.indices) {
            val def = decoder.getOrNull(i) ?: continue
            println("$i $def")
        }
    }
}