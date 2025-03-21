package world.gregs.voidps.tools

import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.config.decoder.IdentityKitDecoder
import world.gregs.voidps.engine.data.Settings

object IdentityKitDefinitions {
    @JvmStatic
    fun main(args: Array<String>) {
        Settings.load()
        val cache: Cache = CacheDelegate(Settings["storage.cache.path"])
        val decoder = IdentityKitDecoder().load(cache)
        println(decoder.lastIndex)
        for (i in decoder.indices) {
            val def = decoder.getOrNull(i) ?: continue
            println("$i $def")
        }
    }
}