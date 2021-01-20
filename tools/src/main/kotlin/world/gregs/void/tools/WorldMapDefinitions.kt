package world.gregs.void.tools

import org.koin.core.context.startKoin
import world.gregs.void.engine.client.cacheDefinitionModule
import world.gregs.void.engine.client.cacheModule
import world.gregs.void.cache.definition.decoder.WorldMapDetailsDecoder

object WorldMapDefinitions {
    @JvmStatic
    fun main(args: Array<String>) {
        val koin = startKoin {
            fileProperties("/tool.properties")
            modules(cacheModule, cacheDefinitionModule)
        }.koin
        val decoder = WorldMapDetailsDecoder(koin.get())
        for (i in decoder.indices) {
            val def = decoder.getOrNull(i) ?: continue
            println(def)
        }
    }
}