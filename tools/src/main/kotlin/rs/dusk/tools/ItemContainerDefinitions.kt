package rs.dusk.tools

import org.koin.core.context.startKoin
import rs.dusk.cache.cacheDefinitionModule
import rs.dusk.cache.cacheModule
import rs.dusk.cache.config.decoder.ItemContainerDecoder

object ItemContainerDefinitions {
    @JvmStatic
    fun main(args: Array<String>) {
        startKoin {
            fileProperties("/tool.properties")
            koin.setProperty("cachePath", "C:\\Users\\Greg\\Downloads\\rs718_cache\\")
            modules(cacheModule, cacheDefinitionModule)
        }
        val decoder = ItemContainerDecoder()
        for (i in 0 until decoder.size) {
            val def = decoder.get(i) ?: continue
            println(def)
        }
    }
}