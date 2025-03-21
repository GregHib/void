package world.gregs.voidps.tools

import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.config.decoder.PlayerVariableParameterDecoder
import world.gregs.voidps.cache.definition.decoder.VarBitDecoder
import world.gregs.voidps.engine.data.Settings

object VarBitDefinitions {
    @JvmStatic
    fun main(args: Array<String>) {
        Settings.load()
        val cache: Cache = CacheDelegate(Settings["storage.cache.path"])
        val decoder = VarBitDecoder().load(cache)
        val varpDecoder = PlayerVariableParameterDecoder().load(cache)
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