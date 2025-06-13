package world.gregs.voidps.tools.cache

import com.displee.cache.CacheLibrary
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.network.file.generatePrefetchKeys

object PrefetchKeyGeneration {

    @JvmStatic
    fun main(args: Array<String>) {
        Settings.load()
        val cache = CacheLibrary(Settings["storage.cache.path"])
        print(cache)
    }

    fun print(cache: CacheLibrary) {
        val keys = generatePrefetchKeys(CacheDelegate(cache))
        println("prefetch.keys=${keys.joinToString(",")}")
    }
}
