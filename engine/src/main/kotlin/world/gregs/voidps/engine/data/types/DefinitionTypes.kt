package world.gregs.voidps.engine.data.types

import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.type.CacheCodec
import world.gregs.voidps.cache.type.Type
import world.gregs.voidps.engine.data.ConfigFiles
import world.gregs.voidps.engine.data.Settings

interface DefinitionTypes<T : Type> {
    var types: Array<T>

    fun get(id: Int) = types[id]

    fun getOrNull(id: Int) = types.getOrNull(id)

    /**
     * Loads definitions from game [cache] with caching to binary file [tempName]
     * Invalidates on cache changes.
     */
    fun CacheCodec<T>.read(
        cache: Cache,
        files: ConfigFiles,
        tempName: String,
        maxDefCacheSize: Int = 1_000_000,
    ) {
        val caching = Settings["storage.caching.active", false]
        val temp = Settings["storage.caching.path"]
        types = read(cache, "${temp}${tempName}", caching, files.cacheUpdate, maxDefCacheSize)
    }
}