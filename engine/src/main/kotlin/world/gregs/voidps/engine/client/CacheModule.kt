package world.gregs.voidps.engine.client

import org.koin.dsl.module
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.secure.Huffman

@Suppress("USELESS_CAST")
@Deprecated("Not in use")
val cacheModule = module {
    single(createdAtStart = true) {
        CacheDelegate(getProperty("cachePath")) as Cache
    }
    single { Huffman(get()) }
}