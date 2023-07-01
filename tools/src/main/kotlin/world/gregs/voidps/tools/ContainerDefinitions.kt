package world.gregs.voidps.tools

import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.config.decoder.ContainerDecoder

object ContainerDefinitions {
    @JvmStatic
    fun main(args: Array<String>) {
        val cache: Cache = CacheDelegate("${System.getProperty("user.home")}\\Downloads\\rs718_cache\\")
        val decoder = ContainerDecoder(cache)
        for (i in 0 until decoder.last) {
            val def = decoder.getOrNull(i) ?: continue
            println(def)
        }
    }
}