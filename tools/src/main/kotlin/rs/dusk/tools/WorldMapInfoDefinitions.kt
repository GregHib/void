package rs.dusk.tools

import org.koin.core.context.startKoin
import rs.dusk.cache.config.decoder.WorldMapInfoDecoder
import rs.dusk.engine.client.cacheDefinitionModule
import rs.dusk.engine.client.cacheModule

object WorldMapInfoDefinitions {
    @JvmStatic
    fun main(args: Array<String>) {
        val koin = startKoin {
            fileProperties("/tool.properties")
            modules(cacheModule, cacheDefinitionModule)
        }.koin
        val decoder = WorldMapInfoDecoder(koin.get())
        for (i in decoder.indices) {
            val def = decoder.getOrNull(i) ?: continue
            if(def.clientScript == 947) {
                println(def.clientScript)
            }
        }
    }
}