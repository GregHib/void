package world.gregs.voidps.tools.convert

import org.koin.core.context.startKoin
import org.koin.dsl.module
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.config.decoder.ContainerDecoder
import world.gregs.voidps.cache.definition.decoder.ItemDecoder
import world.gregs.voidps.engine.client.cacheDefinitionModule

object ContainerConverter {
    @Suppress("USELESS_CAST")
    @JvmStatic
    fun main(args: Array<String>) {

        val cache667 = module {
            single { CacheDelegate("./data/cache/") as Cache }
        }
        val cache718 = module {
            single { CacheDelegate("${System.getProperty("user.home")}\\Downloads\\rs718_cache\\") as Cache }
        }
        val koin = startKoin {
            modules(cacheDefinitionModule)
        }.koin
        koin.loadModules(listOf(cache718))

        println(koin.get<Cache>())
        var decoder = ContainerDecoder(koin.get())

        val containers = (0 until decoder.size).map { it to decoder.getOrNull(it) }.toMap()

        koin.unloadModules(listOf(cache718))
        koin.loadModules(listOf(cache667))

        val itemDecoder = ItemDecoder(koin.get())
        decoder = ContainerDecoder(koin.get())
        var counter = 0
        for (i in 0 until decoder.size) {
            val def = decoder.getOrNull(i)
            val cont = containers[i]
            if(def == null || cont == null) {
                continue
            }

            if(def.length != cont.length) {
//                println("Length changed $i")
            }

            if(cont.ids != null) {
                counter++
                println("$i ${cont.ids!!.mapIndexed { index, it -> "${itemDecoder.getOrNull(it)?.name} ${cont.amounts!![index]}" }.joinToString(separator = ", ")}")
            }
//            println(def)
//            println(cont)
        }
        println("Shops: $counter")
    }
}