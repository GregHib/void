package world.gregs.voidps.tools

import org.koin.core.context.startKoin
import world.gregs.voidps.cache.definition.decoder.ItemDecoder
import world.gregs.voidps.engine.client.cacheDefinitionModule
import world.gregs.voidps.engine.client.cacheModule

object ItemDefinitions {
    @JvmStatic
    fun main(args: Array<String>) {
        val koin = startKoin {
            fileProperties("/tool.properties")
            modules(cacheModule, cacheDefinitionModule)
        }.koin
        val decoder = ItemDecoder(koin.get())
        println(decoder.last)
        for (i in decoder.indices) {
            val def = decoder.getOrNull(i) ?: continue
            if(def.params?.containsKey(686) == true) {
                println("$i ${def.name} ${def.params?.get(686)}")
            }
        }
    }
}