package world.gregs.voidps.tools.cache

import com.displee.cache.CacheLibrary
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.network.file.generatePrefetchKeys

object PrefetchKeyGeneration {

    @JvmStatic
    fun main(args: Array<String>) {
        val cache = CacheLibrary("./data/cache/")
        print(cache)
    }

    fun print(cache: CacheLibrary) {
        val keys = generatePrefetchKeys(CacheDelegate(cache))
        println("prefetch.keys=${keys.joinToString(",")}")
    }
}