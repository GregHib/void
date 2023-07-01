package world.gregs.voidps.tools

import org.koin.core.context.startKoin
import org.koin.fileProperties
import world.gregs.voidps.cache.definition.decoder.WorldMapDetailsDecoder
import world.gregs.voidps.engine.client.cacheModule

object WorldMapDefinitions {
    @JvmStatic
    fun main(args: Array<String>) {
        val koin = startKoin {
            fileProperties("/tool.properties")
            modules(cacheModule)
        }.koin
        val decoder = WorldMapDetailsDecoder(koin.get())
        for (i in decoder.indices) {
            val def = decoder.getOrNull(i) ?: continue
            println(def)
        }
    }
}