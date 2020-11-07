package rs.dusk.tools

import org.koin.core.context.startKoin
import rs.dusk.cache.definition.decoder.ItemDecoder
import rs.dusk.engine.client.cacheDefinitionModule
import rs.dusk.engine.client.cacheModule

object ItemDefinitions {
    @JvmStatic
    fun main(args: Array<String>) {
        val koin = startKoin {
            fileProperties("/tool.properties")
            modules(cacheModule, cacheDefinitionModule)
        }.koin
        val decoder = ItemDecoder(koin.get())
        var count = 0
        println(decoder.size)
        val names = mutableSetOf<String>()
        for (i in 0 until decoder.size) {
            val def = decoder.getOrNull(i) ?: continue
            names.add(def.name)
            count++
//            if(def.params?.containsKey(1) == true) {
//                println("Found $i")
//            }
        }
        println(names.size)
        println(count)
    }
}