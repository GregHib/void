package world.gregs.voidps.tools

import org.koin.core.context.startKoin
import org.koin.fileProperties
import world.gregs.voidps.cache.config.decoder.WorldMapInfoDecoder
import world.gregs.voidps.engine.client.cacheDefinitionModule
import world.gregs.voidps.engine.client.cacheModule

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
            println(def)
        }
    }
}