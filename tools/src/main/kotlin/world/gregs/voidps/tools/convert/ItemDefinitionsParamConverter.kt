package world.gregs.voidps.tools.convert

import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.fileProperties
import world.gregs.voidps.buffer.write.BufferWriter
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.Index.ITEMS
import world.gregs.voidps.cache.definition.decoder.ItemDecoderFull
import world.gregs.voidps.cache.definition.encoder.ItemEncoder

object ItemDefinitionsParamConverter {
    @Suppress("USELESS_CAST")
    @JvmStatic
    fun main(args: Array<String>) {
        val cache667 = module {
            single { CacheDelegate("${System.getProperty("user.home")}/Downloads/rs634_cache/") as Cache }
        }
        val cache718 = module {
            single { CacheDelegate("${System.getProperty("user.home")}/Downloads/rs718_cache/") as Cache }
        }

        val koin = startKoin {
            fileProperties("/tool.properties")
            modules(cache718)
        }.koin

        val decoder718 = ItemDecoder718().loadCache(koin.get())
        val definitions = decoder718.indices.mapNotNull { decoder718.getOrNull(it) }.associateBy { it.id }

        koin.unloadModules(listOf(cache718))
        koin.loadModules(listOf(cache667))

        var count = 0
        var itemCount = 0
        val itemDecoder = ItemDecoderFull()
        val decoder = itemDecoder.loadCache(koin.get())
        val cache = koin.get<Cache>() as CacheDelegate
        val encoder = ItemEncoder()
        for (id in decoder.indices) {
            val def = decoder.getOrNull(id) ?: continue
            val def718 = definitions[id] ?: continue
            val params718 = def718.params ?: continue
            val params = def.params?.toMutableMap() ?: mutableMapOf()
            def.params = params
            var modified = false
            for ((key, value) in params718) {
                if (!params.containsKey(key)) {
                    params[key] = value
                    count++
                    modified = true
                }
            }
            if (modified) {
                val writer = BufferWriter(capacity = 512)
                with(encoder) {
                    writer.encode(def)
                }
                cache.write(ITEMS, itemDecoder.getArchive(id), itemDecoder.getFile(id), writer.toArray())
                itemCount++
            }
        }
        println("Writing changes to cache...")
        cache.update()
        println("$count parameters transferred from $itemCount item definitions.")
    }
}