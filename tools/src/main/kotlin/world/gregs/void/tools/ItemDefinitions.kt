package world.gregs.void.tools

import org.koin.core.context.startKoin
import world.gregs.void.cache.definition.decoder.ItemDecoder
import world.gregs.void.engine.client.cacheDefinitionModule
import world.gregs.void.engine.client.cacheModule

object ItemDefinitions {
    @JvmStatic
    fun main(args: Array<String>) {
        val koin = startKoin {
            fileProperties("/tool.properties")
            modules(cacheModule, cacheDefinitionModule)
        }.koin
        val decoder = ItemDecoder(koin.get())
        for (i in 0 until decoder.size) {
            val def = decoder.getOrNull(i) ?: continue
            if(def.name.contains("chameleon", true)) {
                println("$i ${def.name}")
            }
        }
    }
}