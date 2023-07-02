package world.gregs.voidps.tools

import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.config.decoder.PlayerVariableParameterDecoder
import world.gregs.voidps.cache.definition.decoder.VarBitDecoder

object VarBitDefinitions {
    @JvmStatic
    fun main(args: Array<String>) {
        val cache: Cache = CacheDelegate(property("cachePath"))
        val decoder = VarBitDecoder().loadCache(cache)
        val varpDecoder = PlayerVariableParameterDecoder().loadCache(cache)
        for (i in decoder.indices) {
            val def = decoder.getOrNull(i) ?: continue
            println(def)
        }
        for (i in varpDecoder.indices) {
            val def = varpDecoder.getOrNull(i) ?: continue
            println(def)
        }
    }
}