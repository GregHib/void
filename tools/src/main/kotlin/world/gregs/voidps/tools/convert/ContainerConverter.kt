package world.gregs.voidps.tools.convert

import org.koin.core.context.startKoin
import org.koin.dsl.module
import world.gregs.voidps.buffer.write.BufferWriter
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.Configs.CONTAINERS
import world.gregs.voidps.cache.Indices
import world.gregs.voidps.cache.config.decoder.ContainerDecoder
import world.gregs.voidps.cache.config.encoder.ContainerEncoder
import world.gregs.voidps.cache.definition.decoder.ItemDecoder
import world.gregs.voidps.engine.client.cacheDefinitionModule
import world.gregs.voidps.engine.get

object ContainerConverter {
    @Suppress("USELESS_CAST")
    @JvmStatic
    fun main(args: Array<String>) {
        val cacheModule = module {
            single { CacheDelegate("./data/cache/") as Cache }
        }
        val cache718Module = module {
            single { CacheDelegate("${System.getProperty("user.home")}\\Downloads\\rs718_cache\\") as Cache }
        }
        val koin = startKoin {
            modules(cacheDefinitionModule)
        }.koin
        koin.loadModules(listOf(cache718Module))
        var decoder = ContainerDecoder(koin.get())

        val containers = (0 until decoder.last).associateWith { decoder.getOrNull(it) }

        koin.unloadModules(listOf(cache718Module))
        koin.loadModules(listOf(cacheModule))
        val encoder = ContainerEncoder()
        val cache: Cache = get()

        val itemDecoder = ItemDecoder(cache)
        decoder = ContainerDecoder(cache)
        var counter = 0
        for (i in 0 until decoder.last) {
            val def = decoder.getOrNull(i)
            val cont = containers[i]
            if (def == null || cont == null) {
                continue
            }

            if (def.length != cont.length) {
//                println("Length changed $i ${def.length} ${cont.length} ${cont.ids?.mapIndexed { index, it -> "${itemDecoder.getOrNull(it)?.name} ${cont.amounts!![index]}" }?.joinToString(separator = ", ")}")
            }

            if (cont.ids != null) {
                def.ids = cont.ids?.filter { itemDecoder.getOrNull(it) != null }?.toIntArray()
                def.amounts = cont.amounts!!.take(def.ids!!.size).toIntArray()
                def.length = def.ids!!.size
                counter++
                val writer = BufferWriter(4096)
                with(encoder) {
                    writer.encode(def)
                }
                cache.write(Indices.CONFIGS, CONTAINERS, i, writer.toArray())

                println("$i ${cont.ids!!.mapIndexed { index, it -> "${itemDecoder.getOrNull(it)?.name} ${cont.amounts!![index]}" }.joinToString(separator = ", ")}")
            }
        }
        cache.update()
        println("Shops: $counter")
    }
}