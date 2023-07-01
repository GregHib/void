package world.gregs.voidps.tools.convert

import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.fileProperties
import world.gregs.voidps.buffer.write.BufferWriter
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.Indices.ITEMS
import world.gregs.voidps.cache.definition.decoder.ItemDecoder
import world.gregs.voidps.cache.definition.encoder.ItemEncoder

object ItemDefinitionsParamConverter {
    @JvmStatic
    fun main(args: Array<String>) {

        val cache667 = module {
            single { CacheDelegate("./data/cache/") }
        }
        val cache718 = module {
            single { CacheDelegate("${System.getProperty("user.home")}\\Downloads\\rs718_cache\\") }
        }

        val koin = startKoin {
            fileProperties("/tool.properties")
            modules(cache718)
        }.koin

        val decoder718 = ItemDecoder718(koin.get())
        val definitions = decoder718.indices.mapNotNull { decoder718.getOrNull(it) }.associateBy { it.id }

        koin.unloadModules(listOf(cache718))
        koin.loadModules(listOf(cache667))

        var count = 0
        var itemCount = 0
        val decoder = ItemDecoder(koin.get())
        val cache = koin.get<Cache>() as CacheDelegate
        val encoder = ItemEncoder()
        for (id in decoder.indices) {
            val def = decoder.getOrNull(id) ?: continue
            val def718 = definitions[id] ?: continue
            val params718 = def718.params ?: continue
            val params = def.params?.toMutableMap() ?: hashMapOf()
            if (def.params == null) {
                def.params = params
            }
            var modified = false
            for ((key, value) in params718) {
                if (!params.containsKey(key) || (params.containsKey(key) && params[key] != value)) {
                    params[key] = value
                    val writer = BufferWriter()
                    with(encoder) {
                        writer.encode(def)
                    }
                    cache.write(ITEMS, decoder.getArchive(id), decoder.getFile(id), writer.toArray())
                    count++
                    modified = true
                }
            }
            if (modified) {
                itemCount++
            }
        }
        println("Writing changes to cache...")
        cache.update()
        println("$count parameters transferred from $itemCount item definitions.")
    }
}