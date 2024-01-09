package world.gregs.voidps.cache.memory

import world.gregs.voidps.cache.Cache

interface CacheLoader {

    fun load(path: String, xteas: Map<Int, IntArray>? = null): Cache
}